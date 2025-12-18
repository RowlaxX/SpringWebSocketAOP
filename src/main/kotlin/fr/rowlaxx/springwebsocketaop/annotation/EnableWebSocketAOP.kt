package fr.rowlaxx.springwebsocketaop.annotation

import fr.rowlaxx.marketdata.lib.websocket.service.aop.WebSocketBeanFactoryService
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(WebSocketBeanFactoryService::class)
annotation class EnableWebSocketAOP()
