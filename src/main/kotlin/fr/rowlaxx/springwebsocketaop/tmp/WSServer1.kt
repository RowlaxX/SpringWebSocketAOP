package fr.rowlaxx.springwebsocketaop.tmp

import fr.rowlaxx.springwebsocketaop.annotation.OnAvailable
import fr.rowlaxx.springwebsocketaop.annotation.OnMessage
import fr.rowlaxx.springwebsocketaop.annotation.OnUnavailable
import fr.rowlaxx.springwebsocketaop.annotation.WebSocketServer
import fr.rowlaxx.springwebsocketaop.utils.AutoWebSocketCollection
import fr.rowlaxx.springwebsocketaop.model.WebSocket
import org.slf4j.LoggerFactory

@WebSocketServer(paths = ["/ws1"])
class WSServer1 {
    private val log = LoggerFactory.getLogger(javaClass)

    private val wss = AutoWebSocketCollection()

    @OnAvailable
    fun onAvailable(webSocket: WebSocket) {
        log.info("WsServer1 onAvailable")
        wss.sendAll("Joined ${webSocket.id}")
    }

    @OnUnavailable
    fun onUnavailable(webSocket: WebSocket) {
        log.info("WsServer1 onUnavailable")
        wss.sendAll("Left ${webSocket.id}")
    }

    @OnMessage
    fun onMessage(webSocket: WebSocket, message: String) {
        log.info("WsServer1 onMessage : $message")
    }

}