package fr.rowlaxx.springksocket.util

import fr.rowlaxx.springksocket.data.WebSocketAttributes
import java.net.URI
import java.net.http.HttpHeaders

object WebSocketMapAttributesUtils {

    fun getOrCreateAttributes(attr: MutableMap<String, Any>): WebSocketAttributes {
        return attr.computeIfAbsent("attributes") { _ -> WebSocketAttributes() as Object } as WebSocketAttributes
    }

    fun setAttributes(attr: MutableMap<String, Any>, instance: WebSocketAttributes) {
        attr["attributes"] = instance
    }

    fun getRequestHeaders(attr: MutableMap<String, Any>): HttpHeaders {
        return attr["requestHeaders"] as HttpHeaders
    }

    fun setRequestHeaders(attr: MutableMap<String, Any>, instance: HttpHeaders) {
        attr["requestHeaders"] = instance
    }

    fun getURI(attr: MutableMap<String, Any>): URI {
        return attr["uri"] as URI
    }

    fun setURI(attr: MutableMap<String, Any>, instance: URI) {
        attr["uri"] = instance
    }


}