package de.telekom.usp.mtp.ws

import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.AbstractMessageTransfer
import de.telekom.usp.mtp.MessageTransferEvent
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
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
import kotlinx.coroutines.launch
import okio.ByteString
import okio.ByteString.Companion.toByteString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val USP_WEB_SOCKET_PROTOCOL = "v1.usp"
private const val USP_WEB_SOCKET_EXTENSION = "bbf-usp-protocol"

class WebSocketTransfer(
    private val host: String,
    private val port: Int,
    private val from: EndpointIdentifier,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    engine: HttpClientEngine = CIO.create(),
    pingDuration: Duration = 20.seconds,
    debugMode: Boolean = false
) : AbstractMessageTransfer() {

    private val client = HttpClient(engine) {
        install(Logging) {
            level = if (debugMode) LogLevel.ALL else LogLevel.NONE
            logger = KtorKermitBridge(level)
        }
        install(WebSockets) {
            pingInterval = pingDuration
        }
        developmentMode = debugMode
    }

    private var receiverJob: Job? = null
    private var senderJob: Job? = null

    override suspend fun connect() {
        if (!isConnected()) {
            scope.launch {
                try {
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
                        setConnected(true)
                        emit(MessageTransferEvent.Connected(to = this@WebSocketTransfer))
                        receiverJob = launch { incomingMessagesLoop() }
                        senderJob = launch { outgoingMessagesLoop() }

                        senderJob?.join()
                        receiverJob?.join()
                    }
                } catch (ex: Exception) {
                    Logger.e(throwable = ex) { "Error establishing connectivity of ${this@WebSocketTransfer}" }
                    emit(MessageTransferEvent.ConnectionFailed(this@WebSocketTransfer, ex))
                }
            }
        }
    }

    override suspend fun disconnect() {
        if (isConnected()) {
            client.close()
            senderJob?.cancelAndJoin()
            receiverJob?.cancelAndJoin()
            senderJob = null
            receiverJob = null
            setConnected(false)
            emit(MessageTransferEvent.Disconnected(from = this))
        }
    }

    private suspend fun emit(bytes: ByteString) {
        emit(MessageTransferEvent.BytesReceived(bytes))
    }

    private suspend fun DefaultClientWebSocketSession.incomingMessagesLoop() {
        try {
            Logger.d { "${this@WebSocketTransfer} waiting for incoming frames..." }

            for (frame in incoming) {
                when (frame) {
                    // Note that in non-raw mode, we should never receive Close, Ping or Pong frames
                    is Frame.Binary -> {
                        Logger.d { "${this@WebSocketTransfer} received data frame of size: ${frame.data.size}" }
                        emit(frame.readBytes().toByteString())
                    }

                    else -> {
                        Logger.e { "${this@WebSocketTransfer} received unexpected frame: $frame" }
                    }
                }
            }
            Logger.d { "${this@WebSocketTransfer} frames terminated, cancelling incoming message queue" }

            // When we come here, the connection has been terminated, hence do some cleanup
            disconnect()

        } catch (ex: CancellationException) {
            Logger.d { "Incoming message queue of ${this@WebSocketTransfer} has been cancelled" }
            disconnect()
        } catch (ex: Exception) {
            Logger.e(throwable = ex) { "${this@WebSocketTransfer} error while receiving messages: " + ex::class }
        }
    }

    private suspend fun DefaultClientWebSocketSession.outgoingMessagesLoop() {
        try {
            Logger.d { "${this@WebSocketTransfer} waiting for messages to send..." }

            inputBuffer.collect { bytes ->
                outgoing.send(Frame.Binary(fin = true, data = bytes.toByteArray()))
                Logger.d { "Data frame of size ${bytes.size} sent to $host:$port" }
            }
        } catch (ex: CancellationException) {
            Logger.d { "Outgoing message queue of ${this@WebSocketTransfer} has been cancelled" }
            disconnect()
        } catch (ex: Exception) {
            Logger.e(throwable = ex) { "${this@WebSocketTransfer} error while sending messages: " + ex::class }
        }
    }

    override fun toString(): String {
        return "WebSocket transfer [from: '$from', to: $host:$port]"
    }
}