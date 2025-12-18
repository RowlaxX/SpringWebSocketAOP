package fr.rowlaxx.springwebsocketaop.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WebSocketClientConfiguration(

    val uri: String,
    val pingInterval: Long = 5,
    val pingIntervalUnit: TimeUnit = TimeUnit.SECONDS,
    val connectTimeout: Long = 10,
    val connectTimeoutUnit: TimeUnit = TimeUnit.SECONDS,
    val readTimeout: Long = 10,
    val readTimeoutUnit: TimeUnit = TimeUnit.SECONDS

)
