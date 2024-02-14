package de.telekom.usp

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import okio.ByteString
import okio.ByteString.Companion.toByteString
import kotlin.time.Duration.Companion.seconds

private const val USP_WEB_SOCKET_PROTOCOL = "v1.usp"
private const val USP_WEB_SOCKET_EXTENSION = "bbf-usp-protocol"

class WebSocketConnection(
    private val host: String,
    private val port: Int,
    private val from: EndpointIdentifier,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val developmentMode: Boolean = false
) {

    private val _input = MutableSharedFlow<ByteString>(replay = 2)

    private val _output = MutableSharedFlow<ByteString>()
    val output: SharedFlow<ByteString>
        get() = _output.asSharedFlow()

    private val client = HttpClient {
        developmentMode = this@WebSocketConnection.developmentMode
        install(Logging) {
            logger = io.ktor.client.plugins.logging.Logger.SIMPLE
            level = if (this@WebSocketConnection.developmentMode) LogLevel.ALL else LogLevel.NONE
        }
        install(WebSockets) {
            pingInterval = 10.seconds.inWholeMilliseconds
        }
    }

    private val mutex = Mutex()

    private var isConnected = false

    private var receiverRoutine: Job? = null
    private var senderRoutine: Job? = null

    suspend fun isConnected(): Boolean {
//        mutex.withLock {
        return isConnected
//        }
    }

    private suspend fun connectionState(isConnected: Boolean) {
//        mutex.withLock {
        this.isConnected = isConnected
        Logger.d { "New state of $this is: ${if (isConnected) "CONNECTED" else "DISCONNECTED"}" }
//        }
    }

    suspend fun connect() {
        if (!isConnected()) {
            scope.launch {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = host,
                    port = port,
                    path = "/endpointresource?eid=${from.toShortString()}",
                    request = {
                        headers[HttpHeaders.SecWebSocketProtocol] = USP_WEB_SOCKET_PROTOCOL
                        headers[HttpHeaders.SecWebSocketExtensions] = USP_WEB_SOCKET_EXTENSION
                    }
                ) {
                    connectionState(true)
                    receiverRoutine = launch { incomingMessages() }
                    senderRoutine = launch { sendMessages() }
                }
            }
        }
    }

    suspend fun send(bytes: ByteString) {
        _input.emit(bytes)
        println("Emited bytes to input")
    }

    suspend fun disconnect() {
        if (isConnected()) {
            connectionState(false)
            senderRoutine?.cancelAndJoin()
            receiverRoutine?.cancelAndJoin()
            senderRoutine = null
            receiverRoutine = null
        }
    }

    private suspend fun DefaultClientWebSocketSession.incomingMessages() {
        try {
            for (message in incoming) {
                when (message) {
                    is Frame.Binary -> {
                        Logger.d { "Received data frame of size: ${message.data.size}" }
                        _output.emit(message.readBytes().toByteString())
                    }

                    is Frame.Ping -> {
                        Logger.d { "Received PING message" }
                    }

                    else -> {
                        Logger.d { "Received message of unexpected type: $message" }
                    }
                }
            }
        } catch (ex: CancellationException) {
            //disconnect()
        } catch (ex: Exception) {
            Logger.e(throwable = ex) { "Error while receiving messages: " + ex::class }
        }
    }

    private suspend fun DefaultClientWebSocketSession.sendMessages() {
        try {
            println("111111111111111111111111111111")
            _input.collect { bytes ->
                println("2222222222222222222222222222222222222222")
                Logger.d { "Sending data frame of size ${bytes.size}" }
                outgoing.send(Frame.Binary(fin = true, data = bytes.toByteArray()))
                Logger.d { "Data frame of size ${bytes.size} sent" }
            }
        } catch (ex: CancellationException) {
            disconnect()
        } catch (ex: Exception) {
            Logger.e(throwable = ex) { "Error while sending messages: " + ex::class }
        }
    }

    override fun toString(): String {
        return "WebSocketConnection [from $from to $host:$port]"
    }
}