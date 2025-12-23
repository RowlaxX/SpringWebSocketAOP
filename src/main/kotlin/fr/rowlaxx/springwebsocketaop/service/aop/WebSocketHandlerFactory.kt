package fr.rowlaxx.springwebsocketaop.service.aop

import fr.rowlaxx.springwebsocketaop.annotation.OnAvailable
import fr.rowlaxx.springwebsocketaop.annotation.OnMessage
import fr.rowlaxx.springwebsocketaop.annotation.OnUnavailable
import fr.rowlaxx.springwebsocketaop.data.WebSocketAttributes
import fr.rowlaxx.springwebsocketaop.model.WebSocket
import fr.rowlaxx.springwebsocketaop.model.WebSocketHandler
import fr.rowlaxx.springwebsocketaop.utils.ReflectionUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WebSocketHandlerFactory {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val onAvailableInject = arrayOf(WebSocket::class, WebSocketAttributes::class)
    private val onUnavailableInject = arrayOf(WebSocket::class, WebSocketAttributes::class)
    private val onMessageInject = arrayOf(WebSocket::class, WebSocketAttributes::class, Any::class)

    fun extract(bean: Any): WebSocketHandler {
        val available = ReflectionUtils.findMethodWithAnnotations(bean, OnAvailable::class)
            .map { it.second }
            .map { ReflectionUtils.findInjectionScheme(bean, it, *onAvailableInject) }

        val unavailable = ReflectionUtils.findMethodWithAnnotations(bean, OnUnavailable::class)
            .map { it.second }
            .map { ReflectionUtils.findInjectionScheme(bean, it, *onUnavailableInject) }

        val onMessage = ReflectionUtils.findMethodWithAnnotations(bean, OnMessage::class)
            .map { it.second }
            .map { ReflectionUtils.findInjectionScheme(bean, it, *onMessageInject) }

        if (available.isEmpty() && unavailable.isEmpty()) {
            throw IllegalArgumentException("Bean ${bean::class.simpleName} is not a WebSocketHandler. Please add at least one @OnAvailable, @OnUnavailable or @OnMessage method")
        }

        return InternalImplementation(
            available = available,
            unavailable = unavailable,
            message = onMessage,
        )
    }

    private inner class InternalImplementation(
        private val available: List<ReflectionUtils.InjectionScheme>,
        private val unavailable: List<ReflectionUtils.InjectionScheme>,
        private val message: List<ReflectionUtils.InjectionScheme>,
    ) : WebSocketHandler {

        override fun onAvailable(webSocket: WebSocket) {
            val args = arrayOf(webSocket, webSocket.attributes)

            available.forEach {
                runInWS(it, webSocket, *args)
            }
        }

        override fun onMessage(webSocket: WebSocket, msg: Any) {
            val args = arrayOf(webSocket, webSocket.attributes, msg)

            message.filter { ReflectionUtils.canInject(it, args) }
                .forEach { runInWS(it, webSocket, *args) }
        }

        override fun onUnavailable(webSocket: WebSocket) {
            val args = arrayOf(webSocket, webSocket.attributes)

            unavailable.forEach {
                runInWS(it, webSocket, *args)
            }
        }

        private fun runInWS(scheme: ReflectionUtils.InjectionScheme, ws: WebSocket, vararg args: Any?) {
            runCatching { ReflectionUtils.inject(scheme, *args) }
                .onFailure { log.error("Method ${scheme.method} threw an exception", it) }
                .onSuccess {
                    if (it != null && it != Unit) {
                        ws.sendMessageAsync(it).exceptionally { e ->
                            log.error("Unable to send returned object of type ${it::class.simpleName} : ${e.message}")
                        }
                    }
                }
        }
    }
}