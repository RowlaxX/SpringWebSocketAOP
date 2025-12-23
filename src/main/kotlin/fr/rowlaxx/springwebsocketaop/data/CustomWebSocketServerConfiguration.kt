package fr.rowlaxx.springwebsocketaop.data

import fr.rowlaxx.springwebsocketaop.model.WebSocketDeserializer
import fr.rowlaxx.springwebsocketaop.model.WebSocketHandler
import fr.rowlaxx.springwebsocketaop.model.WebSocketSerializer
import java.net.URI
import java.time.Duration

data class CustomWebSocketServerConfiguration(
    val name: String,

    val handlerChain: List<WebSocketHandler>,
    val serializer: WebSocketSerializer,
    val deserializer: WebSocketDeserializer,

    val initTimeout: Duration,
    val pingAfter: Duration,
    val readTimeout: Duration,
)
