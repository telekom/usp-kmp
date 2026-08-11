/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.mtp.mqtt

import MQTTClient
import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.AbstractMessageTransfer
import de.telekom.usp.mtp.MessageTransferEvent
import de.telekom.usp.mtp.mqtt.kmqtt.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import mqtt.Subscription
import mqtt.packets.mqtt.MQTTConnack
import mqtt.packets.mqtt.MQTTDisconnect
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.MQTT5Connack
import mqtt.packets.mqttv5.MQTT5Properties
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
    webSocketPath: String? = null,
    useTls: Boolean,
    private val from: EndpointIdentifier,
    private val mqttConfig: MqttConfig,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : AbstractMessageTransfer() {

    // Unfortunately the MQTT client library uses polling to retrieve published messages, hence we
    // need to run it in a polling loop.
    private val pollingInterval = 50.milliseconds

    private val connectProperties = MQTT5Properties()

    private val publishingProperties = MQTT5Properties()

    private val remoteTopic: Topic
        get() = overrideTopic ?: mqttConfig.fixedRemoteTopic

    // See R-MQTT.8
    private val clientId: String
        get() = if (mqttConfig.clientId.isNullOrBlank()) {
            if (mqttConfig.version == Version.Mqtt3_1_1) {
                from.toShortString()
            } else {
                ""
            }
        } else {
            mqttConfig.clientId
        }

    private val subscribeTopics = mutableListOf<Topic>()

    private var overrideTopic: Topic? = null

    // TODO: refactor the MQTTClient into an interface to make this class testable
    private val client = MQTTClient(
        mqttVersion = mqttConfig.version.toMQTTVersion(),
        address = host,
        port = port,
        userName = user,
        password = password?.toByteArray()?.toUByteArray(),
        tls = if (useTls) TLSClientSettings() else null,
        webSocket = webSocketPath,
        clientId = clientId,
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

        // R-MQTT.27: USP Endpoints sending a USP Record using MQTT 5.0 MUST have “usp.msg” in the Content Type property.
        publishingProperties.contentType = USP_CONTENT_TYPE

        // R-MQTT.22, R-MQTT.23: include own topic in response topic
        publishingProperties.responseTopic = mqttConfig.ownTopic.value

        // R-MQTT.13: An MQTT 5.0 USP Endpoint MUST include a User Property name-value pair in the CONNECT packet with name of “usp-endpoint-id”
        connectProperties.addUserProperty("usp-endpoint-id" to from.toShortString())
    }

    override suspend fun connect() {
        if (!isConnected()) {
            receiverJob = scope.launch {
                try {
                    subscribe()  // This will create the server connection!
                    notifyConnect()
                    while (client.running) {
                        delay(pollingInterval)
                        client.step()
                    }
                } catch (ex: SocketClosedException) {
                    Logger.d(throwable = ex) { "MQTT socket closed, client no longer connected" }
                } catch (ex: CancellationException) {
                    Logger.d { "MQTT polling session cancelled" }
                } catch (ex: Exception) {
                    Logger.e(throwable = ex) { "Error handling MQTT socket connection" }
                }

                notifyDisconnect()
            }
            senderJob = scope.launch {
                inputBuffer.collect {
                    try {
                        Logger.d { "${this@MqttTransfer} waiting for messages to send..." }

                        inputBuffer.collect { bytes ->
                            val payload = bytes.toByteArray().toUByteArray()
                            client.publish(
                                retain = false,
                                qos = mqttConfig.qos.toQos(),
                                topic = remoteTopic.value,
                                payload = payload,
                                properties = publishingProperties
                            )
                            Logger.d { "Payload of size ${bytes.size} sent to topic '$remoteTopic'" }
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

    private suspend fun notifyConnect() {
        Logger.d { "$this is connected to $host, notifying observers..." }
        setConnected(true)
        emit(MessageTransferEvent.Connected(to = this@MqttTransfer))
    }

    private suspend fun notifyDisconnect() {
        Logger.d { "$this is disconnected from $host, notifying observers..." }
        setConnected(false)
        emit(MessageTransferEvent.Disconnected(from = this@MqttTransfer))
    }

    private fun launchEmit(event: MessageTransferEvent) {
        scope.launch {
            emit(event)
        }
    }

    private fun subscribe() {
        val topics = subscribeTopics + mqttConfig.ownTopic
        Logger.d { "Sending subscribe request for: $topics" }
        client.subscribe(topics.map {
            Subscription(
                it.value,
                SubscriptionOptions(mqttConfig.qos.toQos())
            )
        })
    }

    private fun onConnected(connack: MQTTConnack) {
        Logger.d { "MQTT transfer connected, session present: ${connack.connectAcknowledgeFlags.sessionPresentFlag}" }
        if (connack is MQTT5Connack) {
            val newTopics = connack.subscriptionTopics()
            if (newTopics.isNotEmpty()) {
                subscribeTopics.clear()
                subscribeTopics.addAll(connack.subscriptionTopics())
                Logger.d { "MQTT subscribe topics received in CONNACK: $subscribeTopics" }
            }
        }
        // Subscribe to own topic and to the ones collected above:
        subscribe()
    }

    private fun onDisconnected(disconnect: MQTTDisconnect?) {
        Logger.d { "Mqtt transfer disconnected: $disconnect" }
        scope.launch {
            notifyDisconnect()
        }
    }

    private fun publishReceived(publish: MQTTPublish) {
        val replyTo = publish.replyTo()
        if (replyTo.isNotEmpty()) {
            Logger.i { "Client sent new response topic: '$replyTo'" }
            overrideTopic = Topic(replyTo)
        }

        if (publish.isUspContentType()) {
            publish.payload?.let { payload ->
                launchEmit(MessageTransferEvent.BytesReceived(payload.toByteArray().toByteString()))
            } ?: run {
                Logger.w { "Received MQTT publish record with empty payload" }
            }
        } else {
            Logger.w { "Received MQTT record with unknown content type: '${publish.contentType}'" }
        }
    }

    override fun toString(): String {
        return "MQTT transfer [from: '${mqttConfig.ownTopic}', to: '$remoteTopic', server: $host:$port]"
    }

    companion object {
        // R-MQTT.27a
        const val USP_CONTENT_TYPE = "usp.msg"
        const val USP_MIME_TYPE_1 = "application/vnd.bbf.usp.msg"
        const val USP_MIME_TYPE_2 = "application/vnd.usp.msg"     // Not specified, but used
    }
}
