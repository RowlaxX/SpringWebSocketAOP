package fr.rowlaxx.springwebsocketaop.model

import fr.rowlaxx.springwebsocketaop.data.WebSocketClientProperties
import java.time.Duration
import java.util.concurrent.CompletableFuture

interface PerpetualWebSocket {

    val id: Int
    val name: String
    val switchDuration: Duration
    val shiftDuration: Duration
    val initializers: List<WebSocketHandler>
    val handler: PerpetualWebSocketHandler
    val propertiesFactory: () -> WebSocketClientProperties

    fun isConnected(): Boolean
    fun sendMessageAsync(message: Any): CompletableFuture<Unit>

}