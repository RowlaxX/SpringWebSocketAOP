package fr.rowlaxx.springwebsocketaop.annotation

import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class WebSocketInitializer(

    val timeout: Long = 10,
    val timeoutUnit: TimeUnit = TimeUnit.SECONDS

)
