package fr.rowlaxx.marketdata.lib.websocket.model

import java.net.URI
import java.time.Duration
import java.util.concurrent.CompletableFuture

interface WebSocket {

    val id: Long
    val name: String
    val uri: URI
    val handler: WebSocketHandler
    val attributes: WebSocketAttributes
    val pingInterval: Duration
    val connectTimeout: Duration
    val readTimeout: Duration

    fun sendMessageAsync(message: Any): CompletableFuture<Unit>
    fun closeAsync(reason: String="Normal close"): CompletableFuture<Unit>
    fun isClosed(): Boolean
    fun isOpen(): Boolean

}