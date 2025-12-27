package fr.rowlaxx.springwebsocketaop.data

import fr.rowlaxx.springwebsocketaop.model.WebSocketHandler
import java.time.Duration

data class WebSocketServerProperties(
    val name: String,

    val handlerChain: List<WebSocketHandler>,
    val initTimeout: Duration,
    val pingAfter: Duration,
    val readTimeout: Duration,
)
