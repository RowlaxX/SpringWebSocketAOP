package fr.rowlaxx.springwebsocketaop.util

import fr.rowlaxx.springwebsocketaop.data.WebSocketServerProperties
import fr.rowlaxx.springwebsocketaop.service.io.ServerWebSocketFactory
import fr.rowlaxx.springwebsocketaop.util.ByteBufferUtils.getBackingArray
import fr.rowlaxx.springwebsocketaop.util.WebSocketSessionUtils.handleBinaryMessage
import fr.rowlaxx.springwebsocketaop.util.WebSocketSessionUtils.handleClose
import fr.rowlaxx.springwebsocketaop.util.WebSocketSessionUtils.handlePongMessage
import fr.rowlaxx.springwebsocketaop.util.WebSocketSessionUtils.handleTextMessage
import fr.rowlaxx.springwebsocketaop.util.WebSocketSessionUtils.handleTransportError
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.AbstractWebSocketHandler

@Suppress("UNCHECKED_CAST")
class SpringWebSocketHandler(
    private val config: WebSocketServerProperties,
    private val serverWebSocketFactory: ServerWebSocketFactory
) : AbstractWebSocketHandler() {

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        session.handleBinaryMessage(message.payload.getBackingArray())
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        session.handleTextMessage(message.payload)
    }

    override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
        session.handlePongMessage()
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        session.handleTransportError(exception)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        session.handleClose(status)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        serverWebSocketFactory.wrap(
            session = session,
            config = config,
        )
    }

}