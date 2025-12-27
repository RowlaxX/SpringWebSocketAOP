package fr.rowlaxx.springksocket.annotation

import fr.rowlaxx.springksocket.model.WebSocketDeserializer
import fr.rowlaxx.springksocket.model.WebSocketSerializer
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WebSocketHandlerProperties(

    val serializer: KClass<out WebSocketSerializer> = WebSocketSerializer.Null::class,
    val deserializer: KClass<out WebSocketDeserializer> = WebSocketDeserializer.Null::class

)
