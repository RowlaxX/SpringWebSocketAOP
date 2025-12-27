package fr.rowlaxx.springwebsocketaop.annotation

import fr.rowlaxx.springwebsocketaop.conf.WebSocketClientConfiguration
import fr.rowlaxx.springwebsocketaop.conf.WebSocketServerConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(value = [
    WebSocketServerConfiguration::class,
    WebSocketClientConfiguration::class
])
annotation class EnableWebSocketAOP()
