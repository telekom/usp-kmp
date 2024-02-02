package de.telekom.usp.messages

import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.Error
import de.telekom.usp.MessageNotSupported
import de.telekom.usp.SessionContextNotAllowed
import de.telekom.usp.Versions
import de.telekom.usp.messages.RecordDecoderResult.DecoderError
import de.telekom.usp.messages.RecordDecoderResult.Disconnect
import de.telekom.usp.messages.RecordDecoderResult.Message
import de.telekom.usp.messages.RecordDecoderResult.MqttConnect
import de.telekom.usp.messages.RecordDecoderResult.RecordsMissing
import de.telekom.usp.messages.RecordDecoderResult.Retransmit
import de.telekom.usp.messages.RecordDecoderResult.SessionEstablished
import de.telekom.usp.messages.RecordDecoderResult.StompConnect
import de.telekom.usp.messages.RecordDecoderResult.UdsConnect
import de.telekom.usp.messages.RecordDecoderResult.UspError
import de.telekom.usp.messages.RecordDecoderResult.WebSocketConnect
import de.telekom.usp.proto.msg.Msg
import de.telekom.usp.proto.record.DisconnectRecord
import de.telekom.usp.proto.record.MQTTConnectRecord
import de.telekom.usp.proto.record.NoSessionContextRecord
import de.telekom.usp.proto.record.Record
import de.telekom.usp.proto.record.Record.PayloadSecurity
import de.telekom.usp.proto.record.STOMPConnectRecord
import de.telekom.usp.proto.record.SessionContextRecord
import de.telekom.usp.proto.record.UDSConnectRecord
import de.telekom.usp.proto.record.WebSocketConnectRecord
import de.telekom.usp.proto.record.containsRetransmitRequest
import de.telekom.usp.proto.record.hasPayload
import de.telekom.usp.proto.record.isComplete
import de.telekom.usp.proto.record.isSingleRecord
import de.telekom.usp.proto.record.payloadToBufferedSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import okio.ByteString

class MessageConverterImpl(
    private val local: EndpointIdentifier,
    private val remote: EndpointIdentifier,
    private val version: String = Versions.mostRecent,
    private val cache: RecordBuffer = InMemoryRecordBuffer(),
    private val allowSessionContext: Boolean = true,
) : MessageConverter {

    private val _results = MutableSharedFlow<RecordDecoderResult>()
    override val results = _results.asSharedFlow()

    private var currentContext: SessionContext? = null

    private val payloadSecurity: PayloadSecurity
        get() = currentContext?.payloadSecurity ?: PayloadSecurity.PLAINTEXT

    override suspend fun next(data: ByteString) {
        try {
            if (data.size == 0) {
                // Record.ADAPTER.decode([]) would return a default object, which is not what we want!
                post(DecoderError(IllegalArgumentException("Missing USP record data")))
                return
            }

            val record = Record.ADAPTER.decode(data)

            if (!accept(record)) {
                return
            }

            if (record.session_context != null) {
                handleSessionContext(record, record.session_context)
            } else if (record.no_session_context != null) {
                handleNoSessionContext(record.no_session_context)
            } else if (record.websocket_connect != null) {
                handleWebSocketConnect()
            } else if (record.mqtt_connect != null) {
                handleMqttConnect(record.mqtt_connect)
            } else if (record.stomp_connect != null) {
                handleStompConnect(record.stomp_connect)
            } else if (record.uds_connect != null) {
                handleUdsConnect(record.uds_connect)
            } else if (record.disconnect != null) {
                handleDisconnect(record.disconnect)
            }
        } catch (ex: Exception) {
            Logger.w(throwable = ex) { "Error parsing $data" }
            post(DecoderError(ex))
        }
    }

    override fun noSessionContextMessage(msg: Msg): ByteString {
        return toByteString(
            Record(
                version = version,
                to_id = remote.toShortString(),
                from_id = local.toShortString(),
                payload_security = payloadSecurity,
                no_session_context = NoSessionContextRecord(toByteString(msg))
            )
        )
    }

    override fun sessionContextMessage(
        msg: Msg?,
        retransmitId: ULong?,
        useExistingSession: Boolean
    ): ByteString {
        // TODO: implement segmented sending of messages
        contextFromLocal(useExistingSession).let { context ->
            val session = SessionContextRecord(
                session_id = context.sessionId,
                sequence_id = context.incrementLocalSequenceId(),
                expected_id = context.expectedId,
                retransmit_id = retransmitId?.toLong() ?: 0,
                payload = if (msg != null) listOf(Msg.ADAPTER.encodeByteString(msg)) else emptyList()
            )

            return toByteString(
                Record(
                    version = version,
                    to_id = remote.toShortString(),
                    from_id = local.toShortString(),
                    payload_security = payloadSecurity,
                    session_context = session
                )
            )
        }
    }

    override fun disconnect(error: Error): ByteString {
        return toByteString(
            Record(
                version = version,
                to_id = remote.toShortString(),
                from_id = local.toShortString(),
                payload_security = payloadSecurity,
                disconnect = DisconnectRecord(error.name, error.code)
            )
        )
    }

    override fun webSocketConnect(): ByteString {
        return toByteString(
            Record(
                version = version,
                to_id = remote.toShortString(),
                from_id = local.toShortString(),
                payload_security = payloadSecurity,
                websocket_connect = WebSocketConnectRecord()
            )
        )
    }

    override fun mqttConnect(mqttVersion: String, topic: String): ByteString {
        val ver = when (mqttVersion) {
            "3.1.1" -> MQTTConnectRecord.MQTTVersion.V3_1_1
            "5.0" -> MQTTConnectRecord.MQTTVersion.V5
            else -> throw IllegalArgumentException("Unknown MQTT version: $mqttVersion")
        }

        return toByteString(
            Record(
                version = version,
                to_id = remote.toShortString(),
                from_id = local.toShortString(),
                payload_security = payloadSecurity,
                mqtt_connect = MQTTConnectRecord(ver, topic)
            )
        )
    }

    override fun stompConnect(stompVersion: String, destination: String): ByteString {
        val ver = when (stompVersion) {
            "1.2" -> STOMPConnectRecord.STOMPVersion.V1_2
            else -> throw IllegalArgumentException("Unknown STOMP version: $stompVersion")
        }

        return toByteString(
            Record(
                version = version,
                to_id = remote.toShortString(),
                from_id = local.toShortString(),
                payload_security = payloadSecurity,
                stomp_connect = STOMPConnectRecord(ver, destination)
            )
        )
    }

    override fun udsConnect(): ByteString {
        return toByteString(
            Record(
                version = version,
                to_id = remote.toShortString(),
                from_id = local.toShortString(),
                payload_security = payloadSecurity,
                uds_connect = UDSConnectRecord()
            )
        )
    }

    // -- Helper methods ---------------------------------------------------------------------------

    private suspend fun accept(record: Record): Boolean {
        return if (!Versions.isSupported(record.version)) {
            Logger.w { "Rejecting USP record with unsupported version: '${record.version}' ($record)" }
            post(UspError(MessageNotSupported))
            false
        } else if (remote.toShortString() != record.to_id) {
            Logger.w { "[R-E2E.1] Ignoring USP record with wrong to-endpoint ID: expecting=$remote, received=${record.to_id}" }
            false
        } else {
            true
        }
    }

    private suspend fun handleSessionContext(uspRecord: Record, record: SessionContextRecord) {
        if (!allowSessionContext) {
            Logger.w("[R-E2E.6a] This controller is not allowed to establish a session context, rejecting request")
            post(UspError(SessionContextNotAllowed))
            return
        }

        if (uspRecord.payload_security == PayloadSecurity.TLS12) {
            throw UnsupportedOperationException("Payload security TLS12 is not yet supported")
        }

        val context = contextFromRemote(record)
        val sequenceId = record.sequence_id // Do not use the one from the context here!

        // R-E2E.20:
        if (context.isExpected(sequenceId)) {
            handleExpectedSessionContext(context, record)
        } else if (context.isAhead(sequenceId)) {
            Logger.d { "Received record with future sequence ID $sequenceId for $context" }
            cache.put(record)
            val missing = (context.expectedId..<sequenceId).toList()
            post(RecordsMissing(context.sessionId, missing))
        } else {
            Logger.d { "Ignoring record with sequence ID $sequenceId for $context" }
        }


        // If there is a pending record with the new sequence ID in the cache, process it now:
        while (true) {
            val next = cache.fetch(context.sessionId, context.expectedId)
            if (next != null) {
                handleExpectedSessionContext(context, next)
            } else {
                break
            }
        }
    }

    private suspend fun handleExpectedSessionContext(
        context: SessionContext,
        record: SessionContextRecord
    ) {
        if (record.containsRetransmitRequest) {
            post(Retransmit(context.sessionId, record.retransmit_id))
        }

        if (record.hasPayload) {
            if (record.isSingleRecord) {
                cache.clear(record.session_id, record.sequence_id)
                val msg = Msg.ADAPTER.decode(record.payloadToBufferedSource())
                post(Message(msg))
            } else {
                handleSegmentedSessionContext(context, record)
            }
        }

        context.incrementRemoteSequenceId()
    }

    private suspend fun handleSegmentedSessionContext(
        context: SessionContext,
        record: SessionContextRecord
    ) {
        val sessionId = context.sessionId
        val sequenceId = record.sequence_id

        cache.put(record)

        if (record.isComplete) {
            Logger.d { "Received end of segmented record with sequence ID $sequenceId" }

            val source = cache.payloadToBufferedSource(sessionId, sequenceId)
            post(Message(Msg.ADAPTER.decode(source)))
        } else {
            Logger.d { "Received non-terminal segmented record with sequence ID $sequenceId" }
        }
    }

    /**
     * Returns the session context for the fromId and the specified session context record. Either
     * returns the existing session context when it has a matching session ID or a restarted
     * context when the session ID did not match or a new context if non existed previously.
     */
    private suspend fun contextFromRemote(record: SessionContextRecord): SessionContext {
        currentContext?.let { context ->
            return if (context.hasMatchingSession(record.session_id)) {
                context
            } else {
                cache.clearSession(context.sessionId)
                createSession(record.session_id).also { restartedContext ->
                    Logger.d { "[R-E2E.3] Restarted $restartedContext" }
                    post(SessionEstablished(restartedContext, context))
                }
            }
        }

        cache.clearAll()

        return createSession(record.session_id).also { newContext ->
            Logger.d { "[R-E2E.4] Created new $newContext" }
            post(SessionEstablished(newContext))
        }
    }

    private fun contextFromLocal(useExistingSession: Boolean): SessionContext {
        return if (useExistingSession) {
            currentContext ?: createSession()
        } else {
            createSession()
        }
    }

    private fun createSession(newSessionId: Long? = null): SessionContext {
        val sessionId = newSessionId ?: run {
            runBlocking {
                SessionIdFactory.next()
            }
        }

        return SessionContext(local, remote, payloadSecurity, sessionId).also {
            currentContext = it
        }
    }

    private suspend fun handleNoSessionContext(record: NoSessionContextRecord) {
        Logger.d { "Received no session connect record with payload size=${record.payload.size}" }
        post(Message(Msg.ADAPTER.decode(record.payload)))
    }

    private suspend fun handleWebSocketConnect() {
        Logger.d("Received web socket connect record")
        post(WebSocketConnect)
    }

    private suspend fun handleMqttConnect(mqttConnect: MQTTConnectRecord) {
        Logger.d { "Received MQTT record: version=${mqttConnect.version}, topic='${mqttConnect.subscribed_topic}'" }
        post(MqttConnect(mqttConnect.version.toString(), mqttConnect.subscribed_topic))
    }

    private suspend fun handleUdsConnect(udsConnect: UDSConnectRecord) {
        Logger.d("Received unix domain socket connect record")
        post(UdsConnect)
    }

    private suspend fun handleStompConnect(stompConnect: STOMPConnectRecord) {
        Logger.d { "Received Stomp record: version=${stompConnect.version}, destination='${stompConnect.subscribed_destination}'" }
        post(StompConnect(stompConnect.version.toString(), stompConnect.subscribed_destination))
    }

    private suspend fun handleDisconnect(disconnect: DisconnectRecord) {
        Logger.d { "[R-E2E.6b] Received disconnect record: reason='${disconnect.reason}' (${disconnect.reason_code})" }
        post(Disconnect(Error.from(disconnect.reason_code)))
    }

    private suspend fun post(result: RecordDecoderResult) {
        Logger.d { "New record decode result: $result" }
        _results.emit(result)
    }

    private fun toByteString(record: Record) = Record.ADAPTER.encodeByteString(record)

    private fun toByteString(msg: Msg) = Msg.ADAPTER.encodeByteString(msg)
}