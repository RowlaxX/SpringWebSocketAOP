package fr.rowlaxx.springwebsocketaop.annotation

import fr.rowlaxx.springwebsocketaop.model.WebSocketDeserializer
import fr.rowlaxx.springwebsocketaop.model.WebSocketSerializer
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class WebSocketClient(

    val name: String = "",
    val url: String = "",
    val headers: Array<Header> = [],

    val initializer: Array<KClass<*>> = [],
    val serializer: KClass<out WebSocketSerializer> = WebSocketSerializer.Passthrough::class,
    val deserializer: KClass<out WebSocketDeserializer> = WebSocketDeserializer.Passthrough::class,

    val connectTimeout: String = "PT5S",
    val initTimeout: String = "PT10S",
    val pingAfter: String = "PT5S",
    val readTimeout: String = "PT10S",

    val aliveDuration: String = "PT4H",
    val shiftDuration: String = "PT3S",

) {
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Header(
        val name: String,
        val content: String,

        val spelExpression: Boolean = false,
    )
}
