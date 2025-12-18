package fr.rowlaxx.marketdata.lib.websocket.model

import fr.rowlaxx.springwebsocketaop.exception.WebSocketException

interface WebSocketHandler {

    fun onInitError(webSocket: WebSocket, reason: WebSocketException) {}

    fun onOpen(webSocket: WebSocket) {}
    fun onMessage(webSocket: WebSocket, text: String) {}
    fun onClose(webSocket: WebSocket, reason: WebSocketException) {}

}