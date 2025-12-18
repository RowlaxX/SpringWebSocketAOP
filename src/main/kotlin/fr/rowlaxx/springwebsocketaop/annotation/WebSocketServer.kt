package fr.rowlaxx.springwebsocketaop.annotation

import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class WebSocketServer(

    val initializer: KClass<*> = Unit::class,

)
