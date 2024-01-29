package de.telekom.usp.messages

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.Error
import de.telekom.usp.Versions
import de.telekom.usp.proto.msg.Msg
import de.telekom.usp.proto.record.DisconnectRecord
import de.telekom.usp.proto.record.MQTTConnectRecord
import de.telekom.usp.proto.record.NoSessionContextRecord
import de.telekom.usp.proto.record.Record
import de.telekom.usp.proto.record.STOMPConnectRecord
import de.telekom.usp.proto.record.UDSConnectRecord
import de.telekom.usp.proto.record.WebSocketConnectRecord
import okio.ByteString

class RecordEncoderImpl(
    private val from: EndpointIdentifier,
    private val to: EndpointIdentifier,
    private val payloadSecurity: Record.PayloadSecurity,
    private val version: String = Versions.mostRecent,
) : RecordEncoder {

    private val fromStr = from.toShortString()

    private val toStr = to.toShortString()

    private var currentContext: SessionContext? = null

    init {
        if (payloadSecurity == Record.PayloadSecurity.TLS12) {
            throw UnsupportedOperationException("Payload security TLS12 is not yet supported")
        }
    }

    override fun noSessionContextMessage(msg: Msg): ByteString {
        return toByteString(
            Record(
                version = version,
                to_id = toStr,
                from_id = fromStr,
                payload_security = payloadSecurity,
                no_session_context = NoSessionContextRecord(toByteString(msg))
            )
        )
    }

    override fun sessionContextMessage(
        sessionId: Long,
        msg: Msg?,
        retransmitId: Long?
    ): ByteString {
        currentContext = validContextFor(sessionId)
        TODO()
    }

    override fun disconnect(error: Error): ByteString {
        return toByteString(
            Record(
                version = version,
                to_id = toStr,
                from_id = fromStr,
                payload_security = payloadSecurity,
                disconnect = DisconnectRecord(error.name, error.code)
            )
        )
    }

    override fun webSocketConnect(): ByteString {
        return toByteString(
            Record(
                version = version,
                to_id = toStr,
                from_id = fromStr,
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
                to_id = toStr,
                from_id = fromStr,
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
                to_id = toStr,
                from_id = fromStr,
                payload_security = payloadSecurity,
                stomp_connect = STOMPConnectRecord(ver, destination)
            )
        )
    }

    override fun udsConnect(): ByteString {
        return toByteString(
            Record(
                version = version,
                to_id = toStr,
                from_id = fromStr,
                payload_security = payloadSecurity,
                uds_connect = UDSConnectRecord()
            )
        )
    }

    private fun validContextFor(sessionId: Long): SessionContext {
        currentContext?.let { context ->
            return if (context.hasMatchingSession(sessionId)) {
                context
            } else {
                context.restartWith(sessionId)
            }
        }

        return SessionContext(from, payloadSecurity, sessionId)
    }

    private fun toByteString(record: Record) = Record.ADAPTER.encodeByteString(record)

    private fun toByteString(msg: Msg) = Msg.ADAPTER.encodeByteString(msg)
}