package fr.rowlaxx.springksocket.model

interface WebSocketHandler {
    val serializer: WebSocketSerializer
    val deserializer: WebSocketDeserializer

    fun onAvailable(webSocket: WebSocket) {}

    fun onMessage(webSocket: WebSocket, msg: Any)  {}

    fun onUnavailable(webSocket: WebSocket) {}

}