package fr.rowlaxx.springksocket.annotation

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
