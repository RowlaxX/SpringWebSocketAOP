package fr.rowlaxx.springwebsocketaop.annotation

/**
 * Injectable :
 * - ServerHttpRequest
 * - ServerHttpResponse
 * - WebSocketAttributes
 * - Exception
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AfterHandshake()
