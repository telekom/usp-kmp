package de.telekom.usp.mtp.mqtt

import co.touchlab.kermit.Logger
import de.kempmobil.ktor.mqtt.Connected
import de.kempmobil.ktor.mqtt.Disconnected
import de.kempmobil.ktor.mqtt.MqttClient
import de.kempmobil.ktor.mqtt.PublishRequest
import de.kempmobil.ktor.mqtt.QoS
import de.kempmobil.ktor.mqtt.buildFilterList
import de.kempmobil.ktor.mqtt.packet.Connack
import de.kempmobil.ktor.mqtt.packet.Publish
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.MessageTransfer
import de.telekom.usp.mtp.MessageTransferEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okio.ByteString.Companion.toByteString

class KtorMqttTransfer(
    private val host: String,
    private val port: Int,
    user: String? = null,
    pwd: String? = null,
    useTls: Boolean,
    private val from: EndpointIdentifier,
    private val mqttConfig: MqttConfig,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : MessageTransfer {

    // See R-MQTT.8
    private val clientIdentifier: String
        get() = if (mqttConfig.clientId.isNullOrBlank()) {
            if (mqttConfig.version == Version.Mqtt3_1_1) {
                from.toShortString()
            } else {
                ""
            }
        } else {
            mqttConfig.clientId
        }


    private val remoteTopic: Topic
        get() = overrideTopic ?: mqttConfig.fixedRemoteTopic

    private var overrideTopic: Topic? = null

    private val client = MqttClient(host, port) {
        if (useTls) {
            connection {
                tls { }
            }
        }
        username = user
        password = pwd
        clientId = clientIdentifier
        // R-MQTT.12: An MQTT 5.0 USP Endpoint MUST support setting the Request Response Information property to 1:
        requestResponseInformation = true
        userProperties {
            "usp-endpoint-id" to from.toShortString()
        }
    }

    private val _events = MutableSharedFlow<MessageTransferEvent>()
    override val events: SharedFlow<MessageTransferEvent>
        get() = _events.asSharedFlow()

    init {
        scope.launch {
            client.publishedPackets.collect { publish ->
                val event = publish.toMessageTransferEvent()
                if (event != null) {
                    _events.emit(event)
                }
            }
        }
        scope.launch {
            client.connectionState.collect { state ->
                when (state) {
                    is Connected -> _events.emit(MessageTransferEvent.Connected(this@KtorMqttTransfer))
                    is Disconnected -> _events.emit(MessageTransferEvent.Disconnected(this@KtorMqttTransfer))
                }
            }
        }
    }

    override suspend fun send(bytes: okio.ByteString) {
        Logger.d { "Sending publish request to ${remoteTopic.value}" }
        client.publish(PublishRequest(remoteTopic.value) {
            isRetainMessage = false
            desiredQoS = mqttConfig.qos.asQoS()
            contentType = USP_CONTENT_TYPE
            responseTopic = mqttConfig.ownTopic.value
            payload(kotlinx.io.bytestring.ByteString(bytes.toByteArray()))
        }).onFailure {
            Logger.w { "Cannot publish packet: $it" }
        }
    }

    override suspend fun connect() {
        client.connect()
            .onSuccess { connack ->
                client.subscribe(buildFilterList {
                    connack.subscribeTopics().forEach { topic ->
                        add(topic, mqttConfig.qos.asQoS())
                    }
                })
            }
            .onFailure {
                Logger.w { "Cannot connect: $it" }
            }
    }

    override suspend fun disconnect() {
        client.disconnect()
    }

    private fun Publish.toMessageTransferEvent(): MessageTransferEvent? {
        val replyTo = responseTopic?.value ?: ""
        if (replyTo.isNotEmpty()) {
            Logger.i { "Client sent new response topic: '$replyTo'" }
            overrideTopic = Topic(replyTo)
        }

        if (isUspContentType()) {
            return MessageTransferEvent.BytesReceived(payload.toByteArray().toByteString())
        } else {
            Logger.w { "Received MQTT record with unknown content type: '${contentType}'" }
        }
        return null
    }

    private fun Publish.isUspContentType(): Boolean {
        val localContentType = contentType?.value
        return localContentType == null
                || localContentType == USP_CONTENT_TYPE
                || localContentType == USP_MIME_TYPE_1
                || localContentType == USP_MIME_TYPE_2
    }

    private fun Connack.subscribeTopics(): List<String> {
        return buildList {
            add(mqttConfig.ownTopic.toString())
            userProperties.values.forEach { pair ->
                if (pair.name == "subscribe-topic") add(pair.value)
            }
        }
    }

    private fun de.telekom.usp.mtp.mqtt.QoS.asQoS(): QoS {
        return when (this) {
            de.telekom.usp.mtp.mqtt.QoS.AT_MOST_ONCE -> QoS.AT_MOST_ONCE
            de.telekom.usp.mtp.mqtt.QoS.AT_LEAST_ONCE -> QoS.AT_LEAST_ONCE
            de.telekom.usp.mtp.mqtt.QoS.EXACTLY_ONCE -> QoS.EXACTLY_ONE
        }
    }

    override fun toString(): String {
        return "Ktor MQTT transfer [from: '${mqttConfig.ownTopic}', to: '$remoteTopic', server: $host:$port]"
    }

    companion object {
        // R-MQTT.27a
        const val USP_CONTENT_TYPE = "usp.msg"
        const val USP_MIME_TYPE_1 = "application/vnd.bbf.usp.msg"
        const val USP_MIME_TYPE_2 = "application/vnd.usp.msg"     // Not specified, but used
    }
}