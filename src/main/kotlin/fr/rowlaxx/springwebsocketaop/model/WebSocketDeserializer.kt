package fr.rowlaxx.springwebsocketaop.model

interface WebSocketDeserializer {

    fun fromStringOrByteArray(obj: Any): Any

    object Passthrough : WebSocketDeserializer {
        override fun fromStringOrByteArray(obj: Any): Any = obj
    }

    object Null : WebSocketDeserializer {
        override fun fromStringOrByteArray(obj: Any): Any = throw UnsupportedOperationException()
    }
}