package fr.rowlaxx.springwebsocketaop.conf

import fr.rowlaxx.springwebsocketaop.annotation.WebSocketServer
import fr.rowlaxx.springwebsocketaop.data.CustomWebSocketServerConfiguration
import fr.rowlaxx.springwebsocketaop.model.SpringWebSocketHandler
import fr.rowlaxx.springwebsocketaop.model.WebSocketDeserializer
import fr.rowlaxx.springwebsocketaop.model.WebSocketSerializer
import fr.rowlaxx.springwebsocketaop.service.aop.HandshakeInterceptorFactory
import fr.rowlaxx.springwebsocketaop.service.aop.WebSocketHandlerFactory
import fr.rowlaxx.springwebsocketaop.service.io.ServerWebSocketFactory
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import java.time.Duration

@Configuration
@EnableWebSocket
class WebSocketServerConfiguration(
    private val applicationContext: ApplicationContext,
    private val webSocketFactory: ServerWebSocketFactory,
    private val handlerFactory: WebSocketHandlerFactory,
    private val handshakeInterceptorFactory: HandshakeInterceptorFactory
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        val beans = applicationContext.getBeansWithAnnotation<WebSocketServer>().values

        for (bean in beans) {
            val type = AopUtils.getTargetClass(bean)
            val anno = type.getAnnotation(WebSocketServer::class.java)

            if (anno.paths.isEmpty()) {
                throw IllegalArgumentException("@ServerWebSocket must have at least one path : ${bean::class.java}")
            }

            val name = anno.name.ifEmpty { type.simpleName }
            val handlerChain = anno.initializers
                .map { applicationContext.getBean(it.java) }
                .plus(bean)
                .map { handlerFactory.extract(it) }

            val serializer = if (anno.serializer == WebSocketSerializer.Passthrough::class)
                WebSocketSerializer.Passthrough else applicationContext.getBean(anno.serializer.java)
            val deserializer = if (anno.deserializer == WebSocketDeserializer.Passthrough::class)
                WebSocketDeserializer.Passthrough else applicationContext.getBean(anno.deserializer.java)


            val handshakeInterceptor = handshakeInterceptorFactory.extract(handlerChain.first())
            val config = CustomWebSocketServerConfiguration(
                name = name,
                handlerChain = handlerChain,
                serializer = serializer,
                deserializer = deserializer,
                initTimeout = Duration.parse(anno.initTimeout),
                readTimeout = Duration.parse(anno.readTimeout),
                pingAfter = Duration.parse(anno.pingAfter),
            )

            val handler = SpringWebSocketHandler(
                config = config,
                serverWebSocketFactory = webSocketFactory
            )

            registry.addHandler(handler, *anno.paths)
                .apply { if (anno.withSockJS) withSockJS() }
                .apply { if (anno.allowedOriginPatterns.isNotEmpty()) setAllowedOriginPatterns(*anno.allowedOriginPatterns) }
                .apply { addInterceptors(handshakeInterceptor) }
        }
    }

}