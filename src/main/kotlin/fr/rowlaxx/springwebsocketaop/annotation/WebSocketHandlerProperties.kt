package fr.rowlaxx.springwebsocketaop.annotation

import fr.rowlaxx.springwebsocketaop.model.WebSocketDeserializer
import fr.rowlaxx.springwebsocketaop.model.WebSocketSerializer
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WebSocketHandlerProperties(

    val serializer: KClass<out WebSocketSerializer> = WebSocketSerializer.Null::class,
    val deserializer: KClass<out WebSocketDeserializer> = WebSocketDeserializer.Null::class

)
