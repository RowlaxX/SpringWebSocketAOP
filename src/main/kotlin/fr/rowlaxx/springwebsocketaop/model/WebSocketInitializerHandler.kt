package fr.rowlaxx.marketdata.lib.websocket.model

import fr.rowlaxx.springwebsocketaop.exception.WebSocketException

interface WebSocketInitializerHandler {

    fun onConnected(socket: WebSocket, initialization: WebSocketInitializer) {}
    fun onMessage(socket: WebSocket, message: String, initialization: WebSocketInitializer) {}

    fun onInitialized(socket: WebSocket) {}
    fun onCanceled(socket: WebSocket, error: WebSocketException) {}

}