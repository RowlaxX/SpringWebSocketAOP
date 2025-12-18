package fr.rowlaxx.marketdata.lib.websocket.model

import java.net.URI
import java.time.Duration

data class WebSocketConfiguration(
    val uri: URI,
    val name: String = "WebSocket",
    val headers: Map<String, String?>? = null,
    val pingInterval: Duration = Duration.ofSeconds(5),
    val connectTimeout: Duration = Duration.ofSeconds(10),
    val readTimeout: Duration = Duration.ofSeconds(10),
)
