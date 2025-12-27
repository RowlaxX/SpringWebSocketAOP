package fr.rowlaxx.springksocket.annotation

import fr.rowlaxx.springksocket.conf.WebSocketClientConfiguration
import fr.rowlaxx.springksocket.conf.WebSocketServerConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(value = [
    WebSocketServerConfiguration::class,
    WebSocketClientConfiguration::class
])
annotation class EnableKSocket()
