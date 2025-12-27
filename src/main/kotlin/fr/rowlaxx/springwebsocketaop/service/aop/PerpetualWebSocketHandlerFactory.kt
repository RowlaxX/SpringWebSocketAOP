package fr.rowlaxx.springwebsocketaop.service.aop

import fr.rowlaxx.springwebsocketaop.annotation.OnAvailable
import fr.rowlaxx.springwebsocketaop.annotation.OnMessage
import fr.rowlaxx.springwebsocketaop.annotation.OnUnavailable
import fr.rowlaxx.springwebsocketaop.model.PerpetualWebSocket
import fr.rowlaxx.springwebsocketaop.model.PerpetualWebSocketHandler
import fr.rowlaxx.springwebsocketaop.model.WebSocketDeserializer
import fr.rowlaxx.springwebsocketaop.model.WebSocketSerializer
import fr.rowlaxx.springwebsocketaop.utils.ReflectionUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PerpetualWebSocketHandlerFactory() {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val onAvailableInject = arrayOf(PerpetualWebSocket::class)
    private val onUnavailableInject = arrayOf(PerpetualWebSocket::class)
    private val onMessageInject = arrayOf(PerpetualWebSocket::class, Any::class)

    fun extract(bean: Any, serializer: WebSocketSerializer, deserializer: WebSocketDeserializer): PerpetualWebSocketHandler {
        val available = ReflectionUtils.findMethodsWithAnnotation(bean, OnAvailable::class)
            .map { it.second }
            .map { ReflectionUtils.findInjectionScheme(bean, it, *onAvailableInject) }

        val onMessage = ReflectionUtils.findMethodsWithAnnotation(bean, OnMessage::class)
            .map { it.second }
            .map { ReflectionUtils.findInjectionScheme(bean, it, *onMessageInject) }

        val unavailable = ReflectionUtils.findMethodsWithAnnotation(bean, OnUnavailable::class)
            .map { it.second }
            .map { ReflectionUtils.findInjectionScheme(bean, it, *onUnavailableInject) }

        if (available.isEmpty() && unavailable.isEmpty()) {
            throw IllegalArgumentException("Bean ${bean::class.simpleName} is not a PerpetualWebSocketHandler. Please add at least one @OnAvailable, @OnUnavailable or @OnMessage method")
        }

        return InternalImplementation(
            available = available,
            unavailable = unavailable,
            message = onMessage,
            serializer = serializer,
            deserializer = deserializer
        )
    }

    private inner class InternalImplementation(
        override val deserializer: WebSocketDeserializer,
        override val serializer: WebSocketSerializer,
        private val available: List<ReflectionUtils.InjectionScheme>,
        private val unavailable: List<ReflectionUtils.InjectionScheme>,
        private val message: List<ReflectionUtils.InjectionScheme>,
    ) : PerpetualWebSocketHandler {

        override fun onAvailable(webSocket: PerpetualWebSocket) {
            val args = arrayOf(webSocket)

            available.forEach {
                runInWS(it, webSocket, *args)
            }
        }

        override fun onMessage(webSocket: PerpetualWebSocket, msg: Any) {
            val args = arrayOf(webSocket, msg)

            message.filter { ReflectionUtils.canInject(it, *args) }
                .forEach { runInWS(it, webSocket, *args) }
        }

        override fun onUnavailable(webSocket: PerpetualWebSocket) {
            val args = arrayOf(webSocket)

            unavailable.forEach {
                runInWS(it, webSocket, *args)
            }
        }

        private fun runInWS(scheme: ReflectionUtils.InjectionScheme, ws: PerpetualWebSocket, vararg args: Any?) {
            runCatching { ReflectionUtils.inject(scheme, *args) }
                .onFailure { log.error("Method ${scheme.method} threw an exception", it) }
                .onSuccess {
                    if (it != null && it != Unit) {
                        ws.sendMessageAsync(it) // Future is not failable
                    }
                }
        }
    }
}