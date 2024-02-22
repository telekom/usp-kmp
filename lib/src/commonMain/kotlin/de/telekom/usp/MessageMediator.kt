package de.telekom.usp

import co.touchlab.kermit.Logger
import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.RecordDecoderResult
import de.telekom.usp.mtp.ConnectionEvent
import de.telekom.usp.proto.msg.AddResp
import de.telekom.usp.proto.msg.DeleteResp
import de.telekom.usp.proto.msg.DeregisterResp
import de.telekom.usp.proto.msg.Error
import de.telekom.usp.proto.msg.GetInstancesResp
import de.telekom.usp.proto.msg.GetResp
import de.telekom.usp.proto.msg.GetSupportedDMResp
import de.telekom.usp.proto.msg.GetSupportedProtocolResp
import de.telekom.usp.proto.msg.Header
import de.telekom.usp.proto.msg.Msg
import de.telekom.usp.proto.msg.NotifyResp
import de.telekom.usp.proto.msg.OperateResp
import de.telekom.usp.proto.msg.RegisterResp
import de.telekom.usp.proto.msg.SetResp
import de.telekom.usp.proto.msg.addResponse
import de.telekom.usp.proto.msg.deleteResponse
import de.telekom.usp.proto.msg.deregisterResponse
import de.telekom.usp.proto.msg.error
import de.telekom.usp.proto.msg.getInstancesResponse
import de.telekom.usp.proto.msg.getResponse
import de.telekom.usp.proto.msg.getSupportedDmResponse
import de.telekom.usp.proto.msg.getSupportedProtocolResponse
import de.telekom.usp.proto.msg.id
import de.telekom.usp.proto.msg.isError
import de.telekom.usp.proto.msg.isResponse
import de.telekom.usp.proto.msg.notifyResponse
import de.telekom.usp.proto.msg.operateResponse
import de.telekom.usp.proto.msg.registerResponse
import de.telekom.usp.proto.msg.requireType
import de.telekom.usp.proto.msg.setResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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

    suspend fun sendRequest(msg: Msg, onResponse: (GetResp) -> Unit, onError: (Error) -> Unit) {
        msg.requireType(Header.MsgType.GET)
        sendRequest(msg, onResponse, onError, Msg::getResponse)
    }

    suspend fun sendRequest(
        msg: Msg,
        onResponse: (GetSupportedDMResp) -> Unit,
        onError: (Error) -> Unit
    ) {
        msg.requireType(Header.MsgType.GET_SUPPORTED_DM)
        sendRequest(msg, onResponse, onError, Msg::getSupportedDmResponse)
    }

    suspend fun sendRequest(
        msg: Msg,
        onResponse: (GetInstancesResp) -> Unit,
        onError: (Error) -> Unit
    ) {
        msg.requireType(Header.MsgType.GET_INSTANCES)
        sendRequest(msg, onResponse, onError, Msg::getInstancesResponse)
    }

    suspend fun sendRequest(
        msg: Msg,
        onResponse: (GetSupportedProtocolResp) -> Unit,
        onError: (Error) -> Unit
    ) {
        msg.requireType(Header.MsgType.GET_SUPPORTED_PROTO)
        sendRequest(msg, onResponse, onError, Msg::getSupportedProtocolResponse)
    }

    suspend fun sendRequest(msg: Msg, onResponse: (SetResp) -> Unit, onError: (Error) -> Unit) {
        msg.requireType(Header.MsgType.SET)
        sendRequest(msg, onResponse, onError, Msg::setResponse)
    }

    suspend fun sendRequest(msg: Msg, onResponse: (AddResp) -> Unit, onError: (Error) -> Unit) {
        msg.requireType(Header.MsgType.ADD)
        sendRequest(msg, onResponse, onError, Msg::addResponse)
    }

    suspend fun sendRequest(msg: Msg, onResponse: (DeleteResp) -> Unit, onError: (Error) -> Unit) {
        msg.requireType(Header.MsgType.DELETE)
        sendRequest(msg, onResponse, onError, Msg::deleteResponse)
    }

    suspend fun sendRequest(
        msg: Msg,
        onResponse: ((OperateResp) -> Unit)?,
        onError: ((Error) -> Unit)?
    ) {
        msg.requireType(Header.MsgType.OPERATE)
        sendRequest(msg, onResponse, onError, Msg::operateResponse)
    }

    suspend fun sendRequest(
        msg: Msg,
        onResponse: ((NotifyResp) -> Unit)?,
        onError: ((Error) -> Unit)?
    ) {
        msg.requireType(Header.MsgType.NOTIFY)
        sendRequest(msg, onResponse, onError, Msg::notifyResponse)
    }

    suspend fun sendRequest(
        msg: Msg,
        onResponse: (RegisterResp) -> Unit,
        onError: (Error) -> Unit
    ) {
        msg.requireType(Header.MsgType.REGISTER)
        sendRequest(msg, onResponse, onError, Msg::registerResponse)
    }

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

    private fun handleDecoderResult(result: RecordDecoderResult) {
        when (result) {
            is RecordDecoderResult.Message -> {
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
