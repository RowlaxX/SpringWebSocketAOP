package fr.rowlaxx.springwebsocketaop.annotation

/**
 * Injectable
 * - ServerHttpRequest
 * - ServerHttpResponse
 * - WebSocketAttributes
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class BeforeHandshake()
