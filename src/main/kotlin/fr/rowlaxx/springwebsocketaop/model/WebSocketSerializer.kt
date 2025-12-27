package fr.rowlaxx.springwebsocketaop.model

interface WebSocketSerializer {

    fun toStringOrByteArray(obj: Any): Any

    object Passthrough : WebSocketSerializer {
        override fun toStringOrByteArray(obj: Any): Any = obj
    }

    object Null : WebSocketSerializer {
        override fun toStringOrByteArray(obj: Any): Any = throw UnsupportedOperationException()
    }
}