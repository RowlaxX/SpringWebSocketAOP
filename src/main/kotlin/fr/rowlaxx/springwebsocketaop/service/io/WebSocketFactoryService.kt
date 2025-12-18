package fr.rowlaxx.marketdata.lib.websocket.service.io

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fr.rowlaxx.marketdata.common.log.log
import fr.rowlaxx.marketdata.lib.synchronizer.model.Synchronizer
import fr.rowlaxx.marketdata.lib.synchronizer.service.SynchronizerFactoryService
import fr.rowlaxx.springwebsocketaop.exception.WebSocketClosedException
import fr.rowlaxx.springwebsocketaop.exception.WebSocketConnectionException
import fr.rowlaxx.springwebsocketaop.exception.WebSocketCreationException
import fr.rowlaxx.springwebsocketaop.exception.WebSocketException
import fr.rowlaxx.marketdata.lib.websocket.model.*
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.nio.ByteBuffer
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicLong

@Service
class WebSocketFactoryService(
    private val objectMapper: ObjectMapper,
    concurrentMemoryFactoryService: SynchronizerFactoryService
) {
    private val httpClient = HttpClient.newHttpClient()
    private val idCounter = AtomicLong(0)
    private val memory: Synchronizer<Long, InternalWebSocket> = concurrentMemoryFactoryService.create(
        parallelism = 8,
        name = "Websocket IO",
        poll = { _, ws -> ws.poll() },
        shouldRemove = { _, ws -> ws.isClosed() },
    )

    private fun serialize(obj: Any): String {
        if (obj is CharSequence || obj is JsonNode) {
            return obj.toString()
        }

        return objectMapper.writeValueAsString(obj)
    }

    fun create(
        configuration: WebSocketConfiguration,
        handler: WebSocketHandler,
    ) = create(
        uri = configuration.uri,
        name = configuration.name,
        handler = handler,
        pingInterval = configuration.pingInterval,
        connectTimeout = configuration.connectTimeout,
        readTimeout = configuration.readTimeout,
        headers = configuration.headers
    )

    fun create(
        uri: URI,
        name: String,
        headers: Map<String, String?>? = null,
        pingInterval: Duration = Duration.ofSeconds(5),
        connectTimeout: Duration = Duration.ofSeconds(10),
        readTimeout: Duration = Duration.ofSeconds(10),
        handler: WebSocketHandler,
    ): Long {
        if (pingInterval.isNegative || connectTimeout.isNegative || readTimeout.isNegative) {
            throw IllegalArgumentException("Ping, connect and read timeout must be positive")
        }

        val id = idCounter.getAndIncrement()
        log.debug("[{} ({})] Creating : {}", name, id, uri)

        val listener = InternalJavaListener(
            executeAsync = { memory.runAsync(id, it) }
        )

        val websocket = InternalWebSocket(
            id = id,
            name = name,
            handler = handler,
            uri = uri,
            connectTimeout = connectTimeout,
            readTimeout = readTimeout,
            pingInterval = pingInterval,
            serialize = this::serialize,
            runInSync = { memory.runAsync(id) { _ -> it() } }
        )

        memory.put(id, websocket)

        val rawWs = httpClient.newWebSocketBuilder()
            .also { builder -> headers?.let {
                it  .filterValues { e -> e != null }
                    .forEach { (k, v) -> builder.header(k, v)}
            }}
            .connectTimeout(connectTimeout)
            .buildAsync(uri, listener)

        rawWs.exceptionally {
            websocket.closeWith(WebSocketCreationException(it.message ?: "Unknown error"))
            null
        }

        return id
    }

    private object LastIn : WebSocketAttribute<Instant>()
    private object LastOut : WebSocketAttribute<Instant>()

    private class InternalWebSocket(
        override val id: Long,
        override val name: String,
        override val handler: WebSocketHandler,
        override val uri: URI,
        override val pingInterval: Duration,
        override val connectTimeout: Duration,
        override val readTimeout: Duration,
        override val attributes: WebSocketAttributes = WebSocketAttributes(),
        private val serialize: (Any) -> String,
        private val runInSync: (() -> Unit) -> CompletableFuture<Unit>
    ) : WebSocket {
        private var sending = false
        private val sendingQueue = LinkedList<Pair<CompletableFuture<Unit>, () -> CompletableFuture<*>>>()
        private var closedWith: Exception? = null
        private var opened: Boolean = false
        private var javaWebSocket: java.net.http.WebSocket? = null

        init {
            attributes[LastIn] = Instant.now()
            attributes[LastOut] = Instant.now()
        }

        fun accept(ws: java.net.http.WebSocket) {
            if (isClosed() || javaWebSocket != null) {
                return
            }
            log.debug("[{} ({})] Opened", name, id)
            javaWebSocket = ws
            opened = true
            handler.onOpen(this)

            sendingQueue.poll()?.let { doOutNow(it.first, it.second, true) }
        }

        fun acceptMessage(message: String) {
            if (isClosed()) {
                return
            }

            log.debug("[{} ({})] Received message : {}", name, id, message.take(70))
            handler.onMessage(this, message)
        }

        fun closeWith(exception: WebSocketException) {
            if (isClosed()) {
                return
            }

            javaWebSocket?.abort()
            opened = false
            closedWith = exception

            when (exception) {
                is WebSocketClosedException -> {
                    log.debug("[{} ({})] Closed : {}", name, id, exception.message)
                    handler.onClose(this, exception)
                }
                is WebSocketConnectionException -> {
                    log.error("[{} ({})] Connection error : {}", name, id, exception.message)
                    handler.onClose(this, exception)
                }
                is WebSocketCreationException -> {
                    log.error("[{} ({})] Init Error : {}", name, id, exception.message)
                    handler.onInitError(this, exception)
                }
            }
        }

        override fun isClosed(): Boolean {
            return closedWith != null
        }

        override fun isOpen(): Boolean {
            return opened
        }

        override fun closeAsync(reason: String): CompletableFuture<Unit> {
            return runInSync {
                closeWith(WebSocketClosedException("Closed by client : $reason"))
            }
        }

        override fun sendMessageAsync(message: Any): CompletableFuture<Unit> {
            val cf = CompletableFuture<Unit>()
            runInSync {
                doOutNow(
                    result = cf,
                    action = {
                        val text = serialize(message)
                        log.debug("[{} ({})] Sending message : {}", name, id, text.take(50))
                        javaWebSocket!!.sendText(text, true)
                    },
                    redirectToQueue = true
                )
            }
            return cf
        }

        private fun doOutNow(result: CompletableFuture<Unit>, action: () -> CompletableFuture<*>, redirectToQueue: Boolean) {
            if (!isOpen() || sending) {
                if (redirectToQueue) {
                    sendingQueue.add(result to action)
                }
                return
            }

            sending = true
            action().whenComplete { _, e -> runInSync {
                sending = false

                if (e == null) {
                    attributes[LastOut] = Instant.now()
                    result.complete(Unit)
                }
                else {
                    log.error("[{} ({})] Unable to perform out operation", name, id)
                    val ex = if (e is IOException) WebSocketConnectionException("IOException : ${e.message}")
                             else WebSocketClosedException("Exception : ${e.message}")

                    closeWith(ex)
                    result.completeExceptionally(e)
                }

                sendingQueue.poll()?.let {
                    doOutNow(it.first, it.second, true)
                }
            }}
        }

        fun poll() {
            if (!opened || isClosed()) {
                return
            }

            val lastIn = attributes[LastIn]!!
            val lastOut = attributes[LastOut]!!
            val now = Instant.now()

            if (lastIn + readTimeout < now) {
                closeWith(WebSocketClosedException("Read timeout"))
            }
            else if (maxOf(lastIn, lastOut) + pingInterval < now) {
                doOutNow(
                    result = CompletableFuture<Unit>(),
                    action = { javaWebSocket!!.sendPing(ByteBuffer.allocate(0)) },
                    redirectToQueue = false
                )
            }
        }
    }

    private class InternalJavaListener(
        private val executeAsync: ((InternalWebSocket) -> Unit) -> Unit
    ) : java.net.http.WebSocket.Listener {
        private var text: StringBuilder = StringBuilder()

        override fun onText(webSocket: java.net.http.WebSocket, data: CharSequence, lastData: Boolean): CompletionStage<*> {
            val canFree = CompletableFuture<Unit>()

            executeAsync {
                try {
                    it.attributes[LastIn] = Instant.now()

                    if (text.isEmpty() && lastData) {
                        it.acceptMessage(data.toString())
                    }
                    else {
                        text.append(data)

                        if (lastData) {
                            val result = text.toString()
                            text = StringBuilder()
                            it.acceptMessage(result)
                        }
                    }
                } finally {
                    webSocket.request(1)
                    canFree.complete(Unit)
                }
            }

            return canFree
        }

        override fun onClose(webSocket: java.net.http.WebSocket, statusCode: Int, reason: String?): CompletionStage<*>? {
            executeAsync { it.closeWith(WebSocketClosedException("Closed by server : $reason ($statusCode)")) }
            webSocket.request(1)
            return null
        }

        override fun onError(webSocket: java.net.http.WebSocket, error: Throwable?) {
            executeAsync { it.closeWith(
                WebSocketConnectionException(
                    error?.message ?: "Connection error : ${error?.message}"
                )
            ) }
            webSocket.request(1)
        }

        override fun onOpen(webSocket: java.net.http.WebSocket) {
            executeAsync { it.accept(webSocket) }
            webSocket.request(1)
        }

        override fun onPing(webSocket: java.net.http.WebSocket, message: ByteBuffer): CompletionStage<*>? {
            executeAsync { it.attributes[LastIn] = Instant.now() }
            webSocket.request(1)
            return null
        }

        override fun onPong(webSocket: java.net.http.WebSocket, message: ByteBuffer): CompletionStage<*>? {
            executeAsync { it.attributes[LastIn] = Instant.now() }
            webSocket.request(1)
            return null
        }

    }
}