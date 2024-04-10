package de.telekom.usp.mtp

import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.mqtt.Connack
import de.telekom.usp.mtp.mqtt.Message
import de.telekom.usp.mtp.mqtt.MqttClientCallback
import de.telekom.usp.mtp.mqtt.Topic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import mqtt.MQTTVersion
import mqtt.packets.Qos
import socket.tls.TLSClientSettings

@OptIn(ExperimentalUnsignedTypes::class)
class MqttTransfer(
    private val host: String,
    private val port: Int,
    user: String? = null,
    password: String? = null,
    tls: TLSClientSettings? = null,
    private val clientId: String? = null,
    private val from: EndpointIdentifier,
    private val subscribeTopics: MutableSet<Topic> = mutableSetOf(),
    private var replyToTopic: Topic? = null,
    private val qos: Qos = Qos.AT_LEAST_ONCE,
    private val mqttVersion: MQTTVersion = MQTTVersion.MQTT5,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : AbstractMessageTransfer(), MqttClientCallback {

    private var receiverJob: Job? = null
    private var senderJob: Job? = null

    override suspend fun connect() {
        if (!isConnected()) {
            senderJob = scope.launch {
                inputBuffer.collect {
                    try {
                        Logger.d { "${this@MqttTransfer} waiting for messages to send..." }

                        inputBuffer.collect { bytes ->
                            val payload = bytes.toByteArray().toUByteArray()
//                            client.publish(
//                                retain = false,
//                                qos = qos,
//                                topic = replyToTopic!!,
//                                payload = payload,
//                                properties = publishingProperties
//                            )
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
//            client.disconnect(ReasonCode.SUCCESS)
            receiverJob?.cancelAndJoin()
            senderJob?.cancelAndJoin()
        }
    }

    override fun onConnected(connack: Connack) {
        val newTopics = connack.subscriptionTopics
        if (newTopics.isNotEmpty()) {
            Logger.d { "MQTT subscribe topics received in CONNACK: $newTopics" }
            subscribeTopics.addAll(newTopics)
        }
        // R-MQTT.21 - An MQTT 5.0 USP Endpoint that receives Response Information in the CONNACK
        // packet MUST use this as its “reply to” Topic.
        connack.responseInformation?.let { responseInfo ->
            if (replyToTopic == null) {
                replyToTopic = responseInfo
            }
        }

        if (subscribeTopics.isNotEmpty()) {
//            subscribe()
        } else {
            // R-MQTT.16 when there are no topics at all, terminate the session
            Logger.w("MQTT client has no topics to subscribe to nor was one set, hence disconnecting!")
            scope.launch {
                disconnect()
            }
        }

        launchEmit(MessageTransferEvent.Connected(to = this@MqttTransfer))
    }

    override fun onDisconnected() {
        Logger.d { "Mqtt transfer disconnected" }
        launchEmit(MessageTransferEvent.Disconnected(from = this@MqttTransfer))
    }

    override fun onMessage(message: Message) {
        val replyTo = message.responseTopic
        if (replyTo.isNotEmpty() && replyToTopic != replyTo) {
            Logger.i { "Client sent new response topic: '$replyTo'" }
            replyToTopic = replyTo
        }

        if (message.isUspContent()) {
            if (message.payload.size > 0) {
                launchEmit(MessageTransferEvent.BytesReceived(message.payload))
            } else {
                Logger.w { "Received MQTT publish record with empty payload" }
            }
        } else {
            Logger.w { "Received MQTT publish record with unknown content type: '${message.contentType}'" }
        }
    }

    private fun launchEmit(event: MessageTransferEvent) {
        scope.launch {
            emit(event)
        }
    }

    override fun toString(): String {
        return "MqttTransfer [from: '$from', to: '$replyToTopic', server: $host:$port]"
    }
}