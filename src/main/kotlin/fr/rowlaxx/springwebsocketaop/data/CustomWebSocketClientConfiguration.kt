package fr.rowlaxx.springwebsocketaop.data

import fr.rowlaxx.springwebsocketaop.model.WebSocketDeserializer
import fr.rowlaxx.springwebsocketaop.model.WebSocketHandler
import fr.rowlaxx.springwebsocketaop.model.WebSocketSerializer
import fr.rowlaxx.springwebsocketaop.utils.HttpHeadersUtils
import java.net.URI
import java.net.http.HttpHeaders
import java.time.Duration

data class CustomWebSocketClientConfiguration(
    val name: String,
    val uri: URI,
    val headers: HttpHeaders = HttpHeadersUtils.emptyHeaders,

    val handlerChain: List<WebSocketHandler>,
    val serializer: WebSocketSerializer = WebSocketSerializer.Passthrough,
    val deserializer: WebSocketDeserializer = WebSocketDeserializer.Passthrough,

    val initTimeout: Duration = Duration.ofSeconds(10),
    val pingAfter: Duration = Duration.ofSeconds(5),
    val connectTimeout: Duration = Duration.ofSeconds(10),
    val readTimeout: Duration = Duration.ofSeconds(10),
) {
    init {
        if (pingAfter.isNegative) {
            throw IllegalArgumentException("pingInterval must be positive")
        }
        else if (initTimeout.isNegative) {
            throw IllegalArgumentException("initTimeout must be positive")
        }
        else if (connectTimeout.isNegative) {
            throw IllegalArgumentException("connectTimeout must be positive")
        }
        else if (readTimeout.isNegative) {
            throw IllegalArgumentException("readTimeout must be positive")
        }
    }
}
