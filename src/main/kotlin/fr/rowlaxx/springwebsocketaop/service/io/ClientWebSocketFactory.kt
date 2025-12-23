package fr.rowlaxx.springwebsocketaop.service.io

import fr.rowlaxx.springwebsocketaop.data.CustomWebSocketClientConfiguration
import fr.rowlaxx.springwebsocketaop.exception.WebSocketCreationException
import fr.rowlaxx.springwebsocketaop.model.JavaWebSocketListener
import fr.rowlaxx.springwebsocketaop.model.WebSocket
import org.springframework.stereotype.Service
import java.net.http.HttpClient
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Service
class ClientWebSocketFactory(
    private val baseFactory: BaseWebSocketFactory
) {
    private val httpClient = HttpClient.newHttpClient()

    fun connect(config: CustomWebSocketClientConfiguration): WebSocket {
        val impl = InternalImplementation(
            factory = baseFactory,
            config = config
        )

        impl.connect(httpClient, config.connectTimeout)
        return impl
    }

    private class InternalImplementation(
        factory: BaseWebSocketFactory,
        config: CustomWebSocketClientConfiguration
    ) : BaseWebSocketFactory.BaseWebSocket(
        factory = factory,
        uri = config.uri,
        readTimeout = config.readTimeout,
        pingAfter = config.pingAfter,
        name = config.name,
        handlerChain = config.handlerChain,
        serializer = config.serializer,
        deserializer = config.deserializer,
        initTimeout = config.initTimeout,
        requestHeaders = config.headers
    ) {
        private var javaWS: java.net.http.WebSocket? = null

        fun connect(client: HttpClient, timeout: Duration) {
            val listener = JavaWebSocketListener(
                onOpened = { safeAsync { unsafeOpenWith(it) } },
                onError = { safeAsync { unsafeCloseWith(it) } },
                onTextMessage = { acceptMessage(it) },
                onBinaryMessage = { acceptMessage(it) },
                onDataReceived = { onDataReceived() }
            )

            val builder = client.newWebSocketBuilder()
                .connectTimeout(timeout)

            requestHeaders.map()
                .flatMap { it.value.map { v -> it.key to v } }
                .forEach { builder.header(it.first, it.second) }

            builder.buildAsync(uri, listener)
                .exceptionally {
                    safeAsync {
                        unsafeCloseWith(WebSocketCreationException(it.message ?: "Unknown error"))
                    }
                    null
                }
        }

        override fun unsafePingNow(): CompletableFuture<*> {
            return javaWS!!.sendPing(ByteBuffer.allocate(0))
        }

        override fun unsafeSendText(msg: String): CompletableFuture<*> {
            return javaWS!!.sendText(msg, true)
        }

        override fun unsafeSendBinary(msg: ByteArray): CompletableFuture<*> {
            return javaWS!!.sendBinary(ByteBuffer.wrap(msg), true)
        }

        override fun unsafeHandleClose() {
            javaWS!!.abort()
        }

        override fun unsafeHandleOpen(obj: Any) {
            javaWS = obj as java.net.http.WebSocket
        }

    }

}