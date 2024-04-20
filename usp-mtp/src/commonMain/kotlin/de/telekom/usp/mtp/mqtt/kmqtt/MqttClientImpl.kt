package de.telekom.usp.mtp.mqtt.kmqtt

import MQTTClient
import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.mqtt.ClientId
import de.telekom.usp.mtp.mqtt.Message
import de.telekom.usp.mtp.mqtt.MqttClient
import de.telekom.usp.mtp.mqtt.MqttClientCallback
import de.telekom.usp.mtp.mqtt.QoS
import de.telekom.usp.mtp.mqtt.Subscription
import de.telekom.usp.mtp.mqtt.Topic
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
import mqtt.packets.mqtt.MQTTConnack
import mqtt.packets.mqtt.MQTTDisconnect
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.MQTT5Properties
import mqtt.packets.mqttv5.ReasonCode
import mqtt.packets.mqttv5.SubscriptionOptions
import okio.ByteString
import socket.SocketClosedException
import socket.tls.TLSClientSettings
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalUnsignedTypes::class)
class MqttClientImpl(
    private val host: String,
    private val port: Int,
    user: String? = null,
    password: String? = null,
    tls: TLSClientSettings? = null,
    private val clientId: ClientId,
    private val from: EndpointIdentifier,
    private val mqttVersion: MQTTVersion = MQTTVersion.MQTT5,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : MqttClient {

    private val connectProperties = MQTT5Properties().apply {
        // R-MQTT.12: An MQTT 5.0 USP Endpoint MUST support setting the Request Response Information property to 1
        requestResponseInformation = 1u

        // R-MQTT.13: An MQTT 5.0 USP Endpoint MUST include a User Property name-value pair in the CONNECT packet with name of “usp-endpoint-id”
        addUserProperty(MqttClient.PROPERTY_ENDPOINT_ID to from.toShortString())
    }

    private val publishingProperties = MQTT5Properties().apply {

        // R-MQTT.27 - USP Endpoints sending a USP Record using MQTT 5.0 MUST have “usp.msg” in the Content Type property.
        contentType = Message.USP_CONTENT_TYPE
    }

    private val client = MQTTClient(
        mqttVersion = mqttVersion,
        address = host,
        port = port,
        userName = user,
        password = password?.toByteArray()?.toUByteArray(),
        tls = tls,
        clientId = clientId.value,
        properties = connectProperties,
        onConnected = ::onConnected,
        onDisconnected = ::onDisconnected,
        publishReceived = ::publishReceived
    )

    // Unfortunately the MQTT client library uses polling to retrieve published messages, hence we
    // need to run it in a polling loop.
    private val pollingInterval = 50.milliseconds

    private var callback: MqttClientCallback? = null

    private var clientJob: Job? = null

    // --- Interface methods -----------------------------------------------------------------------

    override fun setCallback(callback: MqttClientCallback) {
        this.callback = callback
    }

    override fun connect() {
        clientJob = scope.launch {
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

            callback?.run {
                onDisconnected()
            }
        }
    }

    override fun disconnect() {
        clientJob?.let { job ->
            clientJob = null
            client.disconnect(ReasonCode.SUCCESS)
            scope.launch {
                job.cancelAndJoin()
            }
        }
    }

    override fun subscribeTo(topics: List<Subscription>) {
        Logger.d { "MQTT client subscribing to $topics" }
        client.subscribe(topics.map {
            mqtt.Subscription(
                it.topic.value,
                SubscriptionOptions(it.qoS.toQos())
            )
        })
    }

    override fun unsubscribeFrom(topics: List<Topic>) {
        Logger.d { "MQTT client unsubscribing from $topics" }
        client.unsubscribe(topics.map { it.value })
    }

    override fun publish(topic: Topic, payload: ByteString, retain: Boolean, qoS: QoS) {
        val bytes = payload.toByteArray().toUByteArray()
        client.publish(
            retain = retain,
            qos = qoS.toQos(),
            topic = topic.value,
            payload = bytes,
            properties = publishingProperties
        )
    }

    // --- Helper methods --------------------------------------------------------------------------

    private fun onConnected(connack: MQTTConnack) {
        callback?.run {
            onConnected(connack.toConnack())
        }
    }

    private fun onDisconnected(disconnect: MQTTDisconnect?) {
        callback?.run {
            onDisconnected()
        }
    }

    private fun publishReceived(publish: MQTTPublish) {
        callback?.run {
            onMessage(publish.toMessage())
        }
    }
}