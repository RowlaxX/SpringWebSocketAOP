package fr.rowlaxx.springwebsocketaop.annotation

import fr.rowlaxx.springwebsocketaop.conf.WebSocketServerConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(WebSocketServerConfiguration::class)
annotation class EnableWebSocketAOP()
