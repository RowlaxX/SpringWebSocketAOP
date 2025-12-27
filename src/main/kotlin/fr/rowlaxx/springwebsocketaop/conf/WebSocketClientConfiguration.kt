package fr.rowlaxx.springwebsocketaop.conf

import fr.rowlaxx.springwebsocketaop.annotation.WebSocketClient
import fr.rowlaxx.springwebsocketaop.service.aop.PerpetualWebSocketHandlerFactory
import fr.rowlaxx.springwebsocketaop.service.aop.WebSocketClientPropertiesFactory
import fr.rowlaxx.springwebsocketaop.service.aop.WebSocketHandlerFactory
import fr.rowlaxx.springwebsocketaop.service.aop.WebSocketSerializerDeserializerExtractor
import fr.rowlaxx.springwebsocketaop.service.aop.AutoPerpetualWebSocketManager
import fr.rowlaxx.springwebsocketaop.service.perp.PerpetualWebSocketFactory
import jakarta.annotation.PostConstruct
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class WebSocketClientConfiguration(
    private val applicationContext: ApplicationContext,
    private val perpetualFactory: PerpetualWebSocketFactory,
    private val handlerFactory: WebSocketHandlerFactory,
    private val perpetualHandlerFactory: PerpetualWebSocketHandlerFactory,
    private val perpetualManager: AutoPerpetualWebSocketManager,
    private val propertiesFactory: WebSocketClientPropertiesFactory,
    private val serDesExtractor: WebSocketSerializerDeserializerExtractor
) {

    @PostConstruct
    fun init() {
        val beans = applicationContext.getBeansWithAnnotation<WebSocketClient>().values

        for (bean in beans) {
            val type = AopUtils.getTargetClass(bean)
            val anno = type.getAnnotation(WebSocketClient::class.java) ?: continue

            val name = anno.name.ifEmpty { type.simpleName }
            val initializers = anno.initializers
                .map { applicationContext.getBean(it.java) }
                .map {
                    val (ser, des) = serDesExtractor.extract(anno, bean)
                    handlerFactory.extract(it, ser, des)
                }

            val properties = propertiesFactory.extract(bean)
            val (ser, des) = serDesExtractor.extract(anno, bean)
            val handler = perpetualHandlerFactory.extract(bean, ser, des)
            perpetualManager.initializeIfNotDone(bean)

            val perpWS = perpetualFactory.create(
                name = name,
                initializers = initializers,
                handler = handler,
                propertiesFactory = properties,
                shiftDuration = Duration.parse(anno.shiftDuration),
                switchDuration = Duration.parse(anno.switchDuration)
            )

            perpetualManager.set(bean, perpWS)
        }
    }

}