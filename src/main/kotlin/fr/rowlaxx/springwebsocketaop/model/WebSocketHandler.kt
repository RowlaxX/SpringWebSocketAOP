package fr.rowlaxx.springwebsocketaop.model

interface WebSocketHandler {

    fun onAvailable(webSocket: WebSocket) {}

    fun onMessage(webSocket: WebSocket, msg: Any)  {}

    fun onUnavailable(webSocket: WebSocket) {}

}