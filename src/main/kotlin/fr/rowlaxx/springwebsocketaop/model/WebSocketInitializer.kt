package fr.rowlaxx.marketdata.lib.websocket.model

interface WebSocketInitializer {

    fun complete()
    fun cancel(reason: String? = null)

}