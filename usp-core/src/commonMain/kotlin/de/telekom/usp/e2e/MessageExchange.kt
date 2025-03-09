package de.telekom.usp.e2e

import co.touchlab.kermit.Logger
import de.telekom.usp.NoError
import de.telekom.usp.SessionContextNotAllowed
import de.telekom.usp.messages.MessageConversionResult
import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.proto.*
import de.telekom.usp.mtp.MessageTransfer
import de.telekom.usp.mtp.MessageTransferEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.jvm.JvmName
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MessageExchange(
    private val converter: MessageConverter,
    private val transfer: MessageTransfer,
    private val clock: Clock = Clock.System,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val requestTimeout: Duration = 20.seconds
) {
    // TODO: refactor constructor parameter MessageTransfer into MessageTransferFactory to not allow
    //       access to the MessageTransfer instance from other classes.

    private val pendingRequests = mutableMapOf<String, PendingRequest<*>>()

    private val jobs = mutableListOf<Job>()

    private var remoteAllowsSessionContext = true

    /** Allows waiting for a connection, see [waitForConnecting] */
    private var connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)

    private var isStarted = false

    private val statusMutex = Mutex()

    suspend fun start() {
        statusMutex.withLock {
            if (isStarted) {
                Logger.i("MessageExchange start() called on a started MessageExchange, ignoring request")
                return
            }

            jobs.add(scope.launch {
                converter.results.collect { result ->
                    handleDecoderResult(result)
                }
            })
            jobs.add(scope.launch {
                transfer.events.collect { event ->
                    handleTransferEvent(event)
                }
            })
            jobs.add(scope.launch {
                watchdog()
            })

            connect()
            isStarted = true
        }
    }

    suspend fun stop() {
        statusMutex.withLock {
            jobs.forEach { it.cancelAndJoin() }
            jobs.clear()
            isStarted = false
        }
    }

    suspend fun isStarted(): Boolean {
        return statusMutex.withLock { isStarted }
    }

    @JvmName("sendGetRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: (MessageExchangeFailure) -> Unit,
        onResponse: (GetResp) -> Unit
    ) {
        msg.requireType(Header.MsgType.GET)
        sendRequest(msg, onError, onResponse, Msg::getResponse)
    }

    @JvmName("sendGetSupportedDMRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: (MessageExchangeFailure) -> Unit,
        onResponse: (GetSupportedDMResp) -> Unit
    ) {
        msg.requireType(Header.MsgType.GET_SUPPORTED_DM)
        sendRequest(msg, onError, onResponse, Msg::getSupportedDmResponse)
    }

    @JvmName("sendGetInstancesRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: (MessageExchangeFailure) -> Unit,
        onResponse: (GetInstancesResp) -> Unit
    ) {
        msg.requireType(Header.MsgType.GET_INSTANCES)
        sendRequest(msg, onError, onResponse, Msg::getInstancesResponse)
    }

    @JvmName("sendGetSupportedProtocolRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: (MessageExchangeFailure) -> Unit,
        onResponse: (GetSupportedProtocolResp) -> Unit
    ) {
        msg.requireType(Header.MsgType.GET_SUPPORTED_PROTO)
        sendRequest(msg, onError, onResponse, Msg::getSupportedProtocolResponse)
    }

    @JvmName("sendSetRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: (MessageExchangeFailure) -> Unit,
        onResponse: (SetResp) -> Unit
    ) {
        msg.requireType(Header.MsgType.SET)
        sendRequest(msg, onError, onResponse, Msg::setResponse)
    }

    @JvmName("sendAddRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: (MessageExchangeFailure) -> Unit,
        onResponse: (AddResp) -> Unit
    ) {
        msg.requireType(Header.MsgType.ADD)
        sendRequest(msg, onError, onResponse, Msg::addResponse)
    }

    @JvmName("sendDeleteRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: (MessageExchangeFailure) -> Unit,
        onResponse: (DeleteResp) -> Unit
    ) {
        msg.requireType(Header.MsgType.DELETE)
        sendRequest(msg, onError, onResponse, Msg::deleteResponse)
    }

    @JvmName("sendOperateRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: ((MessageExchangeFailure) -> Unit),
        onResponse: ((OperateResp) -> Unit)?
    ) {
        msg.requireType(Header.MsgType.OPERATE)
        if ((onResponse != null) && !msg.operateRequest.send_resp) {
            throw IllegalArgumentException("When expecting a response, also specify a response handler")
        }
        sendRequest(msg, onError, onResponse, Msg::operateResponse)
    }

    @JvmName("sendNotifyRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: ((MessageExchangeFailure) -> Unit),
        onResponse: ((NotifyResp) -> Unit)?,
    ) {
        msg.requireType(Header.MsgType.NOTIFY)
        if ((onResponse != null) && !msg.notifyRequest.send_resp) {
            throw IllegalArgumentException("When expecting a response, also specify a response handler")
        }
        sendRequest(msg, onError, onResponse, Msg::notifyResponse)
    }

    @JvmName("sendRegisterRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: (MessageExchangeFailure) -> Unit,
        onResponse: (RegisterResp) -> Unit
    ) {
        msg.requireType(Header.MsgType.REGISTER)
        sendRequest(msg, onError, onResponse, Msg::registerResponse)
    }

    @JvmName("sendDeregisterRequest")
    suspend fun sendRequest(
        msg: Msg,
        onError: (MessageExchangeFailure) -> Unit,
        onResponse: (DeregisterResp) -> Unit
    ) {
        msg.requireType(Header.MsgType.DEREGISTER)
        sendRequest(msg, onError, onResponse, Msg::deregisterResponse)
    }

    // --- Helper methods --------------------------------------------------------------------------

    private suspend fun <T> sendRequest(
        msg: Msg,
        onError: ((MessageExchangeFailure) -> Unit),
        onResponse: ((T) -> Unit)?,
        retrieveResponse: (Msg) -> T
    ) {
        if (!isStarted()) {
            throw IllegalStateException("MessageExchange not started, call start() before sending requests")
        }
        waitForConnecting()
        if (!isConnected()) {
            connect()
            waitForConnecting()
        }

        if (connectionState.value == ConnectionState.CONNECTED) {
            if (converter.allowSessionContext && remoteAllowsSessionContext) {
                converter.sessionContextMessage(msg = msg).forEach { transfer.send(it) }
            } else {
                transfer.send(converter.noSessionContextMessage(msg))
            }

            if (onResponse != null) {
                pendingRequests[msg.id] =
                    PendingRequest(
                        onResponse,
                        onError,
                        retrieveResponse,
                        clock.now() + requestTimeout
                    )
            }
        } else {
            scope.launch {
                onError(MessageExchangeFailure.ConnectionFailed)
            }
        }
    }

    private suspend fun handleTransferEvent(event: MessageTransferEvent) {
        when (event) {
            is MessageTransferEvent.BytesReceived -> {
                Logger.d { "Received ${event.bytes.size} bytes from $transfer" }
                converter.next(event.bytes)
            }

            is MessageTransferEvent.Connected -> {
                Logger.d { "Received connection event from $transfer" }
                connectionState.emit(ConnectionState.CONNECTED)
            }

            is MessageTransferEvent.ConnectionFailed -> {
                Logger.d { "Received connection failed from $transfer" }
                connectionState.emit(ConnectionState.DISCONNECTED)
            }

            is MessageTransferEvent.Disconnected -> {
                Logger.d { "Received disconnected from $transfer" }
                connectionState.emit(ConnectionState.DISCONNECTED)
            }
        }
    }

    /**
     * Suspends execution until the connection state is not `CONNECTING`
     */
    private suspend fun waitForConnecting() {
        Logger.d("Waiting for transfer connection to be established...")
        connectionState.first { it != ConnectionState.CONNECTING }
    }

    private fun isConnected() = connectionState.value == ConnectionState.CONNECTED

    private suspend fun connect() {
        connectionState.emit(ConnectionState.CONNECTING)
        scope.launch {
            Logger.i { "Connecting to $transfer" }
            transfer.connect()
        }
    }

    private suspend fun handleDecoderResult(result: MessageConversionResult) {
        Logger.d { "Received message conversion result: $result" }
        when (result) {
            is MessageConversionResult.Message -> handleMessage(result)
            is MessageConversionResult.UspError -> handleUspError(result)
            is MessageConversionResult.Disconnect -> handleDisconnect(result)
            is MessageConversionResult.DecoderError -> handleDecoderError(result)
            is MessageConversionResult.SessionEstablished -> handleSessionEstablished(result)
            else -> {
                Logger.w { "Ignoring decoder result $result for now..." }
            }
        }
    }

    private fun handleMessage(result: MessageConversionResult.Message) {
        val msg = result.msg
        val request = findPendingRequest(msg)

        if (msg.isResponse) {
            if (request != null) {
                scope.launch {
                    request.onResponse(request.responseFor(msg))
                }
                removePendingRequest(msg)
            } else {
                Logger.e { "Received response with unknown message id: $msg" }
            }
        } else if (msg.isError) {
            if (request != null) {
                scope.launch {
                    request.onError(MessageExchangeFailure.ResponseError(msg.error))
                }
                removePendingRequest(msg)
            } else {
                Logger.e { "Received error with unknown message id: $msg" }
            }
        } else {
            Logger.e { "Request handling not yet implemented: ${msg.body?.request}" }
        }
    }

    private fun handleUspError(result: MessageConversionResult.UspError) {
        val error = result.error
        Logger.e { "Client sent error message: $error" }
    }

    private suspend fun handleDisconnect(result: MessageConversionResult.Disconnect) {
        val error = result.error
        if (error.code == SessionContextNotAllowed.code) {
            Logger.i { "Client sent SessionContextNotAllowed message reconnecting..." }
            remoteAllowsSessionContext = false
            transfer.disconnect()
            transfer.connect()
        } else if (error.code == NoError.code) {
            Logger.d { "Client gracefully disconnected" }
            transfer.disconnect()
        } else {
            Logger.e { "Client sent disconnect with an error: $error" }
            transfer.disconnect()
        }
    }

    private fun handleDecoderError(result: MessageConversionResult.DecoderError) {
        Logger.e(throwable = result.cause, messageString = "Internal error decoding a message")
    }

    private fun handleSessionEstablished(result: MessageConversionResult.SessionEstablished) {
        remoteAllowsSessionContext = true // Just in case...
        Logger.d { "USP session established, restarted=${result.isRestarted}" }
    }

    @Suppress("UNCHECKED_CAST")
    private fun findPendingRequest(msg: Msg): PendingRequest<Any>? {
        return (pendingRequests[msg.id] as? PendingRequest<Any>).also {
            Logger.d { "Found pending request for message id: ${msg.id}: $it" }
        }
    }

    private fun removePendingRequest(msg: Msg) {
        Logger.d { "Removing pending request for message id: ${msg.id}" }
        pendingRequests.remove(msg.id)
    }

    private suspend fun watchdog() {
        try {
            while (true) {
                delay(1.seconds)
                Logger.v { "Message exchange watchdog checking for expired requests, candidates: ${pendingRequests.size}" }
                val now = clock.now()
                val iterator = pendingRequests.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (now > entry.value.expirationTime) {
                        entry.value.onError(MessageExchangeFailure.TimeoutOccurred(entry.key))
                        iterator.remove()
                    }
                }
            }
        } catch (ex: CancellationException) {
            Logger.d { "Message exchange watchdog has been terminated" }
        }
    }

    private class PendingRequest<T>(
        val onResponse: (T) -> Unit,
        val onError: (MessageExchangeFailure) -> Unit,
        val responseFor: (Msg) -> T,
        val expirationTime: Instant
    ) {
        override fun toString(): String {
            return "PendingRequest($onResponse, )"
        }
    }
}

private enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED;
}