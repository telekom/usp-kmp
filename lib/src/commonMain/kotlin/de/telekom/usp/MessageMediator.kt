package de.telekom.usp

import co.touchlab.kermit.Logger
import de.telekom.usp.messages.MessageConversionResult
import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.proto.AddResp
import de.telekom.usp.messages.proto.DeleteResp
import de.telekom.usp.messages.proto.DeregisterResp
import de.telekom.usp.messages.proto.Error
import de.telekom.usp.messages.proto.GetInstancesResp
import de.telekom.usp.messages.proto.GetResp
import de.telekom.usp.messages.proto.GetSupportedDMResp
import de.telekom.usp.messages.proto.GetSupportedProtocolResp
import de.telekom.usp.messages.proto.Header
import de.telekom.usp.messages.proto.Msg
import de.telekom.usp.messages.proto.NotifyResp
import de.telekom.usp.messages.proto.OperateResp
import de.telekom.usp.messages.proto.RegisterResp
import de.telekom.usp.messages.proto.SetResp
import de.telekom.usp.messages.proto.addResponse
import de.telekom.usp.messages.proto.deleteResponse
import de.telekom.usp.messages.proto.deregisterResponse
import de.telekom.usp.messages.proto.error
import de.telekom.usp.messages.proto.getInstancesResponse
import de.telekom.usp.messages.proto.getResponse
import de.telekom.usp.messages.proto.getSupportedDmResponse
import de.telekom.usp.messages.proto.getSupportedProtocolResponse
import de.telekom.usp.messages.proto.id
import de.telekom.usp.messages.proto.isError
import de.telekom.usp.messages.proto.isResponse
import de.telekom.usp.messages.proto.notifyResponse
import de.telekom.usp.messages.proto.operateResponse
import de.telekom.usp.messages.proto.registerResponse
import de.telekom.usp.messages.proto.requireType
import de.telekom.usp.messages.proto.setResponse
import de.telekom.usp.mtp.ConnectionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.jvm.JvmName

class MessageMediator(
    private val converter: MessageConverter,
    private val connection: EndpointConnection,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    init {
        scope.launch {
            converter.results.collect { result ->
                handleDecoderResult(result)
            }
        }
        scope.launch {
            connection.events.collect { event ->
                handleConnectionEvent(event)
            }
        }
    }

    private val pendingRequests = mutableListOf<PendingRequest<*>>()

    @JvmName("sendGetRequest")
    suspend fun sendRequest(msg: Msg, onResponse: (GetResp) -> Unit, onError: (Error) -> Unit) {
        msg.requireType(Header.MsgType.GET)
        sendRequest(msg, onResponse, onError, Msg::getResponse)
    }

    @JvmName("sendGetSupportedDMRequest")
    suspend fun sendRequest(
        msg: Msg,
        onResponse: (GetSupportedDMResp) -> Unit,
        onError: (Error) -> Unit
    ) {
        msg.requireType(Header.MsgType.GET_SUPPORTED_DM)
        sendRequest(msg, onResponse, onError, Msg::getSupportedDmResponse)
    }

    @JvmName("sendGetInstancesRequest")
    suspend fun sendRequest(
        msg: Msg,
        onResponse: (GetInstancesResp) -> Unit,
        onError: (Error) -> Unit
    ) {
        msg.requireType(Header.MsgType.GET_INSTANCES)
        sendRequest(msg, onResponse, onError, Msg::getInstancesResponse)
    }

    @JvmName("sendGetSupportedProtocolRequest")
    suspend fun sendRequest(
        msg: Msg,
        onResponse: (GetSupportedProtocolResp) -> Unit,
        onError: (Error) -> Unit
    ) {
        msg.requireType(Header.MsgType.GET_SUPPORTED_PROTO)
        sendRequest(msg, onResponse, onError, Msg::getSupportedProtocolResponse)
    }

    @JvmName("sendSetRequest")
    suspend fun sendRequest(msg: Msg, onResponse: (SetResp) -> Unit, onError: (Error) -> Unit) {
        msg.requireType(Header.MsgType.SET)
        sendRequest(msg, onResponse, onError, Msg::setResponse)
    }

    @JvmName("sendAddRequest")
    suspend fun sendRequest(msg: Msg, onResponse: (AddResp) -> Unit, onError: (Error) -> Unit) {
        msg.requireType(Header.MsgType.ADD)
        sendRequest(msg, onResponse, onError, Msg::addResponse)
    }

    @JvmName("sendDeleteRequest")
    suspend fun sendRequest(msg: Msg, onResponse: (DeleteResp) -> Unit, onError: (Error) -> Unit) {
        msg.requireType(Header.MsgType.DELETE)
        sendRequest(msg, onResponse, onError, Msg::deleteResponse)
    }

    @JvmName("sendOperateRequest")
    suspend fun sendRequest(
        msg: Msg,
        onResponse: ((OperateResp) -> Unit)?,
        onError: ((Error) -> Unit)?
    ) {
        msg.requireType(Header.MsgType.OPERATE)
        sendRequest(msg, onResponse, onError, Msg::operateResponse)
    }

    @JvmName("sendNotifyRequest")
    suspend fun sendRequest(
        msg: Msg,
        onResponse: ((NotifyResp) -> Unit)?,
        onError: ((Error) -> Unit)?
    ) {
        msg.requireType(Header.MsgType.NOTIFY)
        sendRequest(msg, onResponse, onError, Msg::notifyResponse)
    }

    @JvmName("sendRegisterRequest")
    suspend fun sendRequest(
        msg: Msg,
        onResponse: (RegisterResp) -> Unit,
        onError: (Error) -> Unit
    ) {
        msg.requireType(Header.MsgType.REGISTER)
        sendRequest(msg, onResponse, onError, Msg::registerResponse)
    }

    @JvmName("sendDeregisterRequest")
    suspend fun sendRequest(
        msg: Msg,
        onResponse: (DeregisterResp) -> Unit,
        onError: (Error) -> Unit
    ) {
        msg.requireType(Header.MsgType.DEREGISTER)
        sendRequest(msg, onResponse, onError, Msg::deregisterResponse)
    }

    private suspend fun <T> sendRequest(
        msg: Msg,
        onResponse: ((T) -> Unit)?,
        onError: ((Error) -> Unit)?,
        retrieveResponse: (Msg) -> T
    ) {
        connection.connect()
        connection.send(converter.noSessionContextMessage(msg))

        if (onResponse != null && onError != null) {
            val x = PendingRequest(msg.id, onResponse, onError, retrieveResponse)
            pendingRequests.add(x)
        }
    }

    private suspend fun handleConnectionEvent(event: ConnectionEvent) {
        when (event) {
            is ConnectionEvent.BytesReceived -> {
                converter.next(event.bytes)
            }

            else -> {
                Logger.d { " Ignoring connection event $event for now..." }
            }
        }
    }

    private fun handleDecoderResult(result: MessageConversionResult) {
        when (result) {
            is MessageConversionResult.Message -> {
                val msg = result.msg
                if (msg.isResponse) {
                    handleResponse(msg)
                } else if (msg.isError) {
                    handleError(msg)
                } else {
                    handleRequest(msg)
                }
            }

            else -> {
                Logger.d { "Ignoring decoder result $result for now..." }
            }
        }
    }

    private fun handleResponse(msg: Msg) {
        val request = findPendingRequest(msg)
        if (request != null) {
            request.onResponse(request.responseFor(msg))
            removePendingRequest(msg)
        } else {
            Logger.e { "Received a " }
        }
    }

    private fun handleError(msg: Msg) {
        val request = findPendingRequest(msg)
        if (request != null) {
            request.onError(msg.error)
            removePendingRequest(msg)
        } else {
            Logger.e { "Received a " }
        }
    }

    private fun handleRequest(msg: Msg) {
        Logger.e { "Request handling not yet implemented: ${msg.body?.request}" }
    }

    @Suppress("UNCHECKED_CAST")
    private fun findPendingRequest(msg: Msg): PendingRequest<Any>? {
        return pendingRequests.firstOrNull { pending -> pending.messageId == msg.id } as? PendingRequest<Any>
    }

    private fun removePendingRequest(msg: Msg) {
        pendingRequests.removeAll { pending -> pending.messageId == msg.id }
    }

    private data class PendingRequest<T>(
        val messageId: String,
        val onResponse: (T) -> Unit,
        val onError: (Error) -> Unit,
        val responseFor: (Msg) -> T
    )
}
