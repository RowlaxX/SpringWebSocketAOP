package fr.rowlaxx.springksocket.util

import org.springframework.http.HttpHeaders

object HttpHeadersUtils {

    val emptyHeaders = java.net.http.HttpHeaders.of(emptyMap()) {_, _ -> true}

    fun HttpHeaders.toJavaHeaders(): java.net.http.HttpHeaders {
        val pairs = this.headerSet()
            .flatMap { it.value.map { v -> it.key to v } }
            .groupBy({ it.first }, { it.second })

        return java.net.http.HttpHeaders.of(pairs, { _, _ -> true })
    }

}