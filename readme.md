# Spring-K-Socket

Spring-K-Socket is a powerful Kotlin library designed to take the headache out of managing WebSockets in Spring Boot. It moves away from rigid, centralized configurations toward a modular, annotation-driven, and highly available architecture.

Note : This library is built on top of ```spring-boot-starter-websocket```

To enable this library, use ```@EnableKSocket```

# Why Spring-K-Socket?

Standard Spring WebSocket implementations often force you into "Vertical Configuration"—a single, massive ```@Configuration``` class that becomes hard to maintain as your project grows. Handling reconnections, message synchronization, and "keep-alive" logic manually is time-consuming and error-prone.

Spring-K-Socket changes the game by providing:

- Horizontal Configuration: Declare WebSockets as individual beans, just like ```@RestController``` or ```@Service```.
- Annotation-First: Use ```@WebSocketServer``` and ```@WebSocketClient``` for clean, readable code.
- Zero-Downtime Reconnections: Unique High Availability (HA) logic that opens a new connection before the old one expires.
- Automatic Handshaking: Built-in support for post-connection initialization messages.

# Features

## 1. Horizontal Bean Configuration

Stop stuffing every socket into one config file. With this library, your WebSocket logic lives where it belongs: in its own component.

## 2. Annotation-Based Modularity

```kotlin
@WebSocketServer(paths = ["/chat/room"])
class ChatSocketHandler {
    
    @OnMessage
    fun handleMessage(message: String) {
    // Logic here
    }
    
}
```

## 3. High Availability (HA) & Perpetual Connections

Many external WebSocket providers force-close connections after a set duration. Spring-K-Socket monitors these timeouts and establishes a "bridge" connection shortly before the old one dies, ensuring you never miss a message during the handover.

## 4. Initialization Handlers

Need to send an auth token or a "subscribe" JSON as soon as a client connects? Use the initialization hooks to automate the "unleashing" of your socket's purpose.

# Getting Started

## Usage Example - External data stream

```kotlin
/*
 * A new connection will be opened 23 hours after the connection has been opened.
 * When the new connection has succeeded, the old one will be closed after switchDuration.
 * This will guarantee that no message is missed.
 * 
 * Note : during this short period of two concurrent web-socket, a message deduplicator will work, ensuring you handle every message only once
 */
@WebSocketClient(
    url = "wss://api.external-exchange.com/stream",
    shiftDuration = "PT23H", //23 Hours
    switchDuration = "PT3S",
    initializers = [MarketDataClientSubscriber::class],
    defaultSerializer = MySerializer::class,
    defaultDeserializer = MyDeserializer::class,
)
//PerpetualWebSocket, we handle PerpetualWebSocket in here
class MarketDataClient {
    private val perp = AutoPerpetualWebSocket() // For outside context access
     
    @OnAvailable
    //Called when the connection pool size get from 0 to 1
    fun onAvailable() {
        println("Ready")
    }
    
    @OnMessage
    //We can inject Trade thanks to the custom deserializer
    fun onData(payload: Trade) {
        println("New market data: $payload")
    }
    
    @OnUnavailable
    //Called when the connection pool size get from 1 to 0
    fun onUnavailable() {
        //Logic
    }
    
}

@Component
//Initializer, we handle WebSocket in here
class MarketDataClientSubscriber {

    @OnAvailable
    fun onConnected(ws: WebSocket) {
        //The custom serializer will convert the SubscribeRequest to a String or a ByteArray
        ws.sendMessageAsync(SubscribeRequest(
            id = 0,
            topic = "BTCUSDT"   
        ))
    }
    
    @OnMessage
    //We can inject a SubscribeResponse thanks to the custom deserializer
    fun onSubscribed(ws: WebSocket, result: SubscribeResponse) {
        if (result.id == 0) {
            //Moving to the next handler
            ws.completeHandlerAsync()
        }
    }
    
    @OnUnavailable
    fun onUnavailable(ws: WebSocket) {
        // Called when the WebSocket has been closed, or when completeHandlerAsync() has been called
    }
}
```

## Usage Example - Chat Server with rooms


```kotlin
//Declare an attribute that hold the room id
object ChatRoom : WebSocketAttribute<String>()

@WebSocketServer(
    paths = ["/chat"],
    initializers = [WebSocketChatServerInitializer::class],
    defaultSerializer = MySerializer::class,
    defaultDeserializer = MyDeserializer::class,
    initTimeout = "PT10S" // Close connection if they did not arrive to the final handler within 10s
)
//We handle websocket in here
class WebSocketChatServer {
    //This collection will be auto managed by the library
    private val connections = AutoWebSocketCollection()
    
    @OnUnavailable
    fun onLeft(ws: WebSocket) {
        val room = ws.attributes[ChatRoom]
        val msg = LeaveMessage(ws.id)
        connections.send(msg) { ws.attributes[ChatRoom] == room }
    }

    @OnAvailable
    fun onJoined(ws: WebSocket) {
        val room = ws.attributes[ChatRoom]
        val msg = JoinedMessage(ws.id)
        connections.send(msg) { ws.attributes[ChatRoom] == room }
    }
    
    @OnMessage
    fun onMessage(ws: WebSocket, msg: TextMessage) {
        val room = ws.attributes[ChatRoom]
        connections.send(msg) { ws.attributes[ChatRoom] == room }
    }
}

@Component
//We handle websocket in here
class WebSocketChatServerInitializer {
    
    @OnMessage
    fun goToChatRoom(ws: WebSocket, request: ChatRoomRequest) {
        ws.attributes[ChatRoom] = request.room
        ws.completeHandlerAsync()
    }
    
} 
```

## Sending messages from a non WebSocket context

You can use ```AutoPerpetualWebSocket``` and ```AutoWebSocketCollection``` in your handler class to access the WebSocket outside the ```OnAvailable```, ```OnUnavailable``` and ```OnMessage``` callbacks

See the above examples for more info

## Custom Serializer/Deserializer for each handler

You can override the defaultSerializer and defaultDeserializer properties by annotating your handler class by ```@WebSocketHandlerProperties```

## Handling server handshake

On server-side, you can handle server handshake via the ```@BeforeHandshake``` and ```@AfterHandshake``` annotations

Note : these annotation must be present on the **first handler**

# Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are greatly appreciated.

- Fork the Project
- Create your Feature Branch (git checkout -b feature/AmazingFeature)
- Commit your Changes (git commit -m 'Add some AmazingFeature')
- Push to the Branch (git push origin feature/AmazingFeature)
- Open a Pull Request

# License

Distributed under the MIT License. See LICENSE for more information.
