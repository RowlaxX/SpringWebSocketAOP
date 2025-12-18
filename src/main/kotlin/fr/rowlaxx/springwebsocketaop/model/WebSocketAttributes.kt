package fr.rowlaxx.marketdata.lib.websocket.model

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class WebSocketAttributes {
    private val attributes = ConcurrentHashMap<KClass<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: WebSocketAttribute<T>): T? = attributes[key::class] as? T

    operator fun <T> set(key: WebSocketAttribute<T>, value: T) {
        attributes[key::class] = value as Any
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> putIfAbsent(key: WebSocketAttribute<T>, value: Any): T = attributes.computeIfAbsent(key::class) { value } as T
}