package de.telekom.usp.mtp

import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.MessageTransferProtocol
import de.telekom.usp.mtp.util.KtorKermitBridge
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
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
import kotlinx.coroutines.sync.withLock
import okio.ByteString
import okio.ByteString.Companion.toByteString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val USP_WEB_SOCKET_PROTOCOL = "v1.usp"
private const val USP_WEB_SOCKET_EXTENSION = "bbf-usp-protocol"

class WebSocketConnection(
    private val host: String,
    private val port: Int,
    private val from: EndpointIdentifier,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val pingInterval: Duration = 1.minutes,
    private val developmentMode: Boolean = false
) : EndpointConnection {

    override val mtp: MessageTransferProtocol = MessageTransferProtocol.WEB_SOCKET

    private val input = MutableSharedFlow<ByteString>(replay = 10)

    private val _events = MutableSharedFlow<ConnectionEvent>()
    override val events: SharedFlow<ConnectionEvent>
        get() = _events.asSharedFlow()

    private val client = HttpClient {
        developmentMode = this@WebSocketConnection.developmentMode
        install(Logging) {
            level = if (this@WebSocketConnection.developmentMode) LogLevel.ALL else LogLevel.NONE
            logger = KtorKermitBridge(level)
        }
        install(WebSockets) {
            pingInterval = this@WebSocketConnection.pingInterval.inWholeMilliseconds
        }
    }

    private val mutex = Mutex()

    private var isConnected = false

    private var receiverRoutine: Job? = null
    private var senderRoutine: Job? = null

    override suspend fun send(bytes: ByteString) {
        input.emit(bytes)
    }

    override suspend fun connect() {
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
                    connected()
                    receiverRoutine = launch { incomingMessagesLoop() }
                    senderRoutine = launch { outgoingMessagesLoop() }

                    senderRoutine?.join()
                    receiverRoutine?.join()
                }
            }
        }
    }

    override suspend fun disconnect() {
        if (shouldDisconnect()) {
            client.close()
            senderRoutine?.cancelAndJoin()
            receiverRoutine?.cancelAndJoin()
            senderRoutine = null
            receiverRoutine = null
            emit(ConnectionEvent.Disconnected(from = this))
        }
    }

    private suspend fun isConnected(): Boolean {
        mutex.withLock {
            return isConnected
        }
    }

    private suspend fun connected() {
        mutex.withLock {
            this.isConnected = true
            emit(ConnectionEvent.Connected(to = this))
            Logger.d { "New state of $this is: CONNECTED" }
        }
    }

    private suspend fun shouldDisconnect(): Boolean {
        mutex.withLock {
            if (isConnected) {
                isConnected = false
                Logger.d { "New state of $this is: DISCONNECTED" }
                return true
            } else {
                return false
            }
        }
    }

    private suspend fun emit(event: ConnectionEvent) {
        _events.emit(event)
    }

    private suspend fun emit(bytes: ByteString) {
        _events.emit(ConnectionEvent.BytesReceived(bytes))
    }

    private suspend fun DefaultClientWebSocketSession.incomingMessagesLoop() {
        try {
            Logger.d { "${this@WebSocketConnection} waiting for incoming frames..." }

            for (frame in incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        Logger.d { "${this@WebSocketConnection} received data frame of size: ${frame.data.size}" }
                        emit(frame.readBytes().toByteString())
                    }

                    else -> {
                        Logger.e { "${this@WebSocketConnection} received unexpected frame: $frame" }
                    }
                }
            }
        } catch (ex: CancellationException) {
            Logger.d { "Incoming message queue of ${this@WebSocketConnection} has been cancelled" }
            disconnect()
        } catch (ex: Exception) {
            Logger.e(throwable = ex) { "${this@WebSocketConnection} error while receiving messages: " + ex::class }
        }
    }

    private suspend fun DefaultClientWebSocketSession.outgoingMessagesLoop() {
        try {
            Logger.d { "${this@WebSocketConnection} waiting for messages to send..." }

            input.collect { bytes ->
                outgoing.send(Frame.Binary(fin = true, data = bytes.toByteArray()))
                Logger.d { "Data frame of size ${bytes.size} sent to $host:$port" }
            }
        } catch (ex: CancellationException) {
            Logger.d { "Outgoing message queue of ${this@WebSocketConnection} has been cancelled" }
            disconnect()
        } catch (ex: Exception) {
            Logger.e(throwable = ex) { "${this@WebSocketConnection} error while sending messages: " + ex::class }
        }
    }

    override fun toString(): String {
        return "WebSocketConnection [from: '$from' to: $host:$port]"
    }
}