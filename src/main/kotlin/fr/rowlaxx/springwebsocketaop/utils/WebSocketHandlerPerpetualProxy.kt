package fr.rowlaxx.springwebsocketaop.utils

import fr.rowlaxx.springwebsocketaop.model.*

class WebSocketHandlerPerpetualProxy(
    private val acceptOpeningConnection: (WebSocket) -> Boolean,
    private val acceptMessage: (WebSocket, Any) -> Boolean,
    private val acceptClosingConnection: (WebSocket) -> Boolean,
    private val perpetualWebSocket: PerpetualWebSocket,
    private val handler: PerpetualWebSocketHandler
) : WebSocketHandler {
    override val deserializer: WebSocketDeserializer get() = handler.deserializer
    override val serializer: WebSocketSerializer get() = handler.serializer

    override fun onAvailable(webSocket: WebSocket) {
        if (acceptOpeningConnection(webSocket)) {
            handler.onAvailable(perpetualWebSocket)
        }
    }

    override fun onMessage(webSocket: WebSocket, msg: Any) {
        if (acceptMessage(webSocket, msg)) {
            handler.onMessage(perpetualWebSocket, msg)
        }
    }

    override fun onUnavailable(webSocket: WebSocket) {
        if (acceptClosingConnection(webSocket)) {
            handler.onUnavailable(perpetualWebSocket)
        }
    }
}