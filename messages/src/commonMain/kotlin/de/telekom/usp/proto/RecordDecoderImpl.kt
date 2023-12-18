package de.telekom.usp.proto

import co.touchlab.kermit.Logger
import de.telekom.usp.MessageNotSupported
import de.telekom.usp.Versions
import de.telekom.usp.proto.RecordDecoderResult.DecoderError
import de.telekom.usp.proto.RecordDecoderResult.Disconnect
import de.telekom.usp.proto.RecordDecoderResult.Message
import de.telekom.usp.proto.RecordDecoderResult.MqttConnect
import de.telekom.usp.proto.RecordDecoderResult.StompConnect
import de.telekom.usp.proto.RecordDecoderResult.UspError
import de.telekom.usp.proto.RecordDecoderResult.WebSocketConnect
import de.telekom.usp.proto.msg.Msg
import de.telekom.usp.proto.record.DisconnectRecord
import de.telekom.usp.proto.record.MQTTConnectRecord
import de.telekom.usp.proto.record.NoSessionContextRecord
import de.telekom.usp.proto.record.Record
import de.telekom.usp.proto.record.STOMPConnectRecord
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okio.ByteString

class RecordDecoderImpl(private val context: SessionContext) : RecordDecoder {

    private val _results = MutableSharedFlow<RecordDecoderResult>()
    override val results = _results.asSharedFlow()

    override suspend fun next(data: ByteString) {
        try {
            if (data.size == 0) {
                // Record.ADAPTER.decode([]) returns a default object, which is not what we want!
                _results.emit(DecoderError(IllegalArgumentException("Missing USP record data")))
                return
            }

            val record = Record.ADAPTER.decode(data)

            if (!Versions.isSupported(record.version)) {
                Logger.d { "Rejecting USP record with unsupported version: '${record.version}' ($record)" }
                _results.emit(UspError(MessageNotSupported))
                return
            }

            if (!context.isEndpointMatching(record)) {
                Logger.d { "[R-E2E.1] Ignoring USP record with wrong to-endpoint: expecting=${context.to}, received=$record" }
                return
            }

            if (record.payload_security == Record.PayloadSecurity.TLS12) {
                throw UnsupportedOperationException("Payload security TLS12 is not yet supported")
            }

            if (record.session_context != null) {

            } else if (record.no_session_context != null) {
                handleNoSessionConnect(record.no_session_context)
            } else if (record.websocket_connect != null) {
                handleWebSocketConnect()
            } else if (record.mqtt_connect != null) {
                handleMqttConnect(record.mqtt_connect)
            } else if (record.stomp_connect != null) {
                handleStompConnect(record.stomp_connect)
            } else if (record.disconnect != null) {
                handleDisconnect(record.disconnect)
            }
        } catch (ex: Exception) {
            Logger.w(throwable = ex) { "Error parsing $data" }
            _results.emit(DecoderError(ex))
        }
    }

    private suspend fun handleNoSessionConnect(record: NoSessionContextRecord) {
        Logger.d { "Received no session connect record with payload size=${record.payload.size}" }
        _results.emit(Message(Msg.ADAPTER.decode(record.payload)))
    }

    private suspend fun handleWebSocketConnect() {
        Logger.d("Received web socket connect record")
        _results.emit(WebSocketConnect)
    }

    private suspend fun handleMqttConnect(mqttConnect: MQTTConnectRecord) {
        Logger.d { "Received MQTT record: version=${mqttConnect.version}, topic='${mqttConnect.subscribed_topic}'" }
        _results.emit(
            MqttConnect(
                mqttConnect.version.toString(),
                mqttConnect.subscribed_topic
            )
        )
    }

    private suspend fun handleStompConnect(stompConnect: STOMPConnectRecord) {
        Logger.d { "Received Stomp record: version=${stompConnect.version}, destination='${stompConnect.subscribed_destination}'" }
        _results.emit(
            StompConnect(
                stompConnect.version.toString(),
                stompConnect.subscribed_destination
            )
        )
    }

    private suspend fun handleDisconnect(disconnect: DisconnectRecord) {
        Logger.d { "Received disconnect record: reason='${disconnect.reason}' (${disconnect.reason_code})" }
        _results.emit(Disconnect(disconnect.reason, disconnect.reason_code))
    }
}