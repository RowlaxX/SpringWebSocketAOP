package fr.rowlaxx.springwebsocketaop.exception

open class WebSocketException(msg: String, t: Throwable?) : Exception(msg, t)

class WebSocketClosedException(msg: String) : WebSocketException(msg, null)
class WebSocketConnectionException(msg: String) : WebSocketException(msg, null)
class WebSocketCreationException(msg: String) : WebSocketException(msg, null)