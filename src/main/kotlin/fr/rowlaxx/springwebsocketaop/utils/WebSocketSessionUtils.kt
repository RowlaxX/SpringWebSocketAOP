package fr.rowlaxx.springwebsocketaop.utils

import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession

@Suppress("UNCHECKED_CAST")
object WebSocketSessionUtils {

    fun WebSocketSession.setHandleBinaryMessage(run: (ByteArray) -> Unit) {
        attributes["binary"] = run
    }

    fun WebSocketSession.handleBinaryMessage(message: ByteArray) {
        val result = attributes["binary"] as (ByteArray) -> Unit
        result.invoke(message)
    }

    fun WebSocketSession.setHandleTextMessage(run: (String) -> Unit) {
        attributes["text"] = run
    }

    fun WebSocketSession.handleTextMessage(message: String) {
        val result = attributes["text"] as (String) -> Unit
        result.invoke(message)
    }

    fun WebSocketSession.setHandlePongMessage(run: () -> Unit) {
        attributes["pong"] = run
    }

    fun WebSocketSession.handlePongMessage() {
        val result = attributes["pong"] as () -> Unit
        result.invoke()
    }

    fun WebSocketSession.setHandleTransportError(run: (Throwable) -> Unit) {
        attributes["error"] = run
    }

    fun WebSocketSession.handleTransportError(exception: Throwable) {
        val result = attributes["error"] as (Throwable) -> Unit
        result.invoke(exception)
    }

    fun WebSocketSession.setHandleClose(run: (CloseStatus) -> Unit) {
        attributes["close"] = run
    }

    fun WebSocketSession.handleClose(status: CloseStatus) {
        val resul = attributes["close"] as (CloseStatus) -> Unit
        resul.invoke(status)
    }

}