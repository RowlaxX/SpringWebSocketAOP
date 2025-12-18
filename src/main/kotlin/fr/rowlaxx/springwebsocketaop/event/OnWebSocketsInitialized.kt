package fr.rowlaxx.springwebsocketaop.event

import fr.rowlaxx.marketdata.lib.websocket.model.PerpetualWebSocket

data class OnWebSocketsInitialized (
    val beanAndWS: List<Pair<Any, PerpetualWebSocket>>
)