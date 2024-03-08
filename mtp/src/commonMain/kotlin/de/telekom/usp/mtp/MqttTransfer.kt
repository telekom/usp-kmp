package de.telekom.usp.mtp

import MQTTClient
import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.MqttTransfer.Companion.PROPERTY_SUBSCRIBE_TOPIC
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mqtt.MQTTVersion
import mqtt.Subscription
import mqtt.packets.Qos
import mqtt.packets.mqtt.MQTTConnack
import mqtt.packets.mqtt.MQTTDisconnect
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.MQTT5Connack
import mqtt.packets.mqttv5.MQTT5Properties
import mqtt.packets.mqttv5.MQTT5Publish
import mqtt.packets.mqttv5.ReasonCode
import mqtt.packets.mqttv5.SubscriptionOptions
import okio.ByteString.Companion.toByteString
import socket.SocketClosedException
import socket.tls.TLSClientSettings
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalUnsignedTypes::class)
class MqttTransfer(
    private val host: String,
    private val port: Int,
    user: String? = null,
    password: String? = null,
    tls: TLSClientSettings? = null,
    private val clientId: String? = null,
    private val from: EndpointIdentifier,
    private val subscribeTopics: MutableSet<String> = mutableSetOf(),
    private var replyToTopic: String? = null,
    private val qos: Qos = Qos.AT_LEAST_ONCE,
    private val mqttVersion: MQTTVersion = MQTTVersion.MQTT5,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : AbstractMessageTransfer() {

    // Unfortunately the MQTT client library uses polling to retrieve published messages, hence we
    // need to run it in a polling loop.
    private val pollingInterval = 50.milliseconds

    private val connectProperties = MQTT5Properties()

    private val publishingProperties = MQTT5Properties()

    private val client = MQTTClient(
        mqttVersion = mqttVersion,
        address = host,
        port = port,
        userName = user,
        password = password?.toByteArray()?.toUByteArray(),
        tls = tls,
        clientId = clientId.validate(),
        properties = connectProperties,
        onConnected = ::onConnected,
        onDisconnected = ::onDisconnected,
        publishReceived = ::publishReceived
    )

    private var receiverJob: Job? = null
    private var senderJob: Job? = null

    init {
        // R-MQTT.12: An MQTT 5.0 USP Endpoint MUST support setting the Request Response Information property to 1
        publishingProperties.requestResponseInformation = 1u

        // R-MQTT.27 - USP Endpoints sending a USP Record using MQTT 5.0 MUST have “usp.msg” in the Content Type property.
        publishingProperties.contentType = USP_CONTENT_TYPE

        // R-MQTT.13: An MQTT 5.0 USP Endpoint MUST include a User Property name-value pair in the CONNECT packet with name of “usp-endpoint-id”
        connectProperties.addUserProperty(PROPERTY_ENDPOINT_ID to from.toShortString())
    }

    override suspend fun connect() {
        if (!isConnected()) {
            receiverJob = scope.launch {
                try {
                    while (client.running) {
                        client.step()
                        delay(pollingInterval)
                    }
                } catch (ex: SocketClosedException) {
                    Logger.d { "MQTT socket closed, client no longer connected" }
                } catch (ex: CancellationException) {
                    Logger.d { "MQTT polling session cancelled" }
                } catch (ex: Exception) {
                    Logger.e(throwable = ex) { "Error handling MQTT socket connection" }
                }

                launchEmit(MessageTransferEvent.Disconnected(from = this@MqttTransfer))
            }
            senderJob = scope.launch {
                inputBuffer.collect {
                    try {
                        Logger.d { "${this@MqttTransfer} waiting for messages to send..." }

                        inputBuffer.collect { bytes ->
                            val payload = bytes.toByteArray().toUByteArray()
                            client.publish(
                                retain = false,
                                qos = qos,
                                topic = replyToTopic!!,
                                payload = payload,
                                properties = publishingProperties
                            )
                            Logger.d { "Payload of size ${bytes.size} sent to topic '$replyToTopic'" }
                        }
                    } catch (ex: CancellationException) {
                        Logger.d { "Outgoing message queue of ${this@MqttTransfer} has been cancelled" }
                        disconnect()
                    } catch (ex: Exception) {
                        Logger.e(throwable = ex) { "${this@MqttTransfer} error while publishing messages: " + ex::class }
                    }

                }
            }
        }
    }

    override suspend fun disconnect() {
        if (isConnected()) {
            client.disconnect(ReasonCode.SUCCESS)
            receiverJob?.cancelAndJoin()
            senderJob?.cancelAndJoin()
        }
    }

    private fun launchEmit(event: MessageTransferEvent) {
        scope.launch {
            emit(event)
        }
    }

    private fun subscribe() {
        if (subscribeTopics.isNotEmpty()) {
            Logger.d { "MQTT client subscribing to $subscribeTopics" }
            client.subscribe(subscribeTopics.map { Subscription(it, SubscriptionOptions(qos)) })
        }
    }

    private fun unsubscribeFrom(topics: List<String>) {
        if (topics.isNotEmpty()) {
            client.unsubscribe(topics)
        }
    }

    private fun onConnected(connack: MQTTConnack) {
        Logger.d { "MQTT transfer connected, session present: ${connack.connectAcknowledgeFlags.sessionPresentFlag}" }
        if (connack is MQTT5Connack) {
            val newTopics = connack.subscriptionTopics()
            if (newTopics.isNotEmpty()) {
                Logger.d { "MQTT subscribe topics received in CONNACK: $newTopics" }
                subscribeTopics.addAll(newTopics)
            }
            // R-MQTT.21 - An MQTT 5.0 USP Endpoint that receives Response Information in the CONNACK
            // packet MUST use this as its “reply to” Topic.
            connack.properties.responseInformation?.let { responseInfo ->
                if (replyToTopic == null) {
                    replyToTopic = responseInfo
                }
            }
        }

        if (subscribeTopics.isNotEmpty()) {
            subscribe()
        } else {
            // R-MQTT.16 when there are no topics at all, terminate the session
            Logger.w("MQTT client has no topics to subscribe to nor was one set, hence disconnecting!")
            scope.launch {
                disconnect()
            }
        }

        launchEmit(MessageTransferEvent.Connected(to = this@MqttTransfer))
    }

    private fun onDisconnected(disconnect: MQTTDisconnect?) {
        Logger.d { "Mqtt transfer disconnected: $disconnect" }
        launchEmit(MessageTransferEvent.Disconnected(from = this@MqttTransfer))
    }

    private fun publishReceived(publish: MQTTPublish) {
        val replyTo = publish.replyTo()
        if (replyTo.isNotEmpty() && replyToTopic != replyTo) {
            Logger.i { "Client sent new response topic: '$replyTo'" }
            replyToTopic = replyTo
        }

        val contentType = publish.contentType()
        if (contentType == null || contentType == USP_CONTENT_TYPE || contentType == USP_MIME_TYPE) {
            publish.payload?.let { payload ->
                launchEmit(MessageTransferEvent.BytesReceived(payload.toByteArray().toByteString()))
            } ?: run {
                Logger.w { "Received MQTT publish record with empty payload" }
            }
        } else {
            Logger.w { "Received MQTT publish record with unknown content type: '$contentType'" }
        }
    }

    /** Validate the client ID according to R-MQTT.8. */
    private fun String?.validate(): String {
        return if (this.isNullOrBlank()) {
            if (mqttVersion == MQTTVersion.MQTT3_1_1) {
                from.toShortString()
            } else {
                ""
            }
        } else {
            this
        }
    }

    override fun toString(): String {
        return "MqttTransfer [from: '$from', to: '$replyToTopic', server: $host:$port]"
    }

    companion object {
        // R-MQTT.27a
        const val USP_CONTENT_TYPE = "usp.msg"
        const val USP_MIME_TYPE = "application/vnd.bbf.usp.msg"

        // R-MQTT.13
        const val PROPERTY_ENDPOINT_ID = "usp-endpoint-id"

        // R-MQTT.15
        const val PROPERTY_SUBSCRIBE_TOPIC = "subscribe-topic"
    }
}

private fun MQTTPublish.replyTo(): String {
    return if (this is MQTT5Publish) {
        // R-MQTT.23 - USP Endpoints using MQTT 5.0 MUST include their “reply to” Topic in the PUBLISH Response Topic property.
        properties.responseTopic ?: ""
    } else {
        // R-MQTT.24 - USP Endpoints using MQTT 3.1.1 MUST include their “reply to” Topic after “/reply-to=” at the end of the PUBLISH Topic Name
        topicName.substringAfter("/reply-to=", "")
    }
}

private fun MQTTPublish.contentType(): String? {
    return if (this is MQTT5Publish) {
        properties.contentType
    } else {
        null
    }
}

private fun MQTT5Connack.subscriptionTopics() =
    properties.userProperty.filter { it.first == PROPERTY_SUBSCRIBE_TOPIC }.map { it.second }
