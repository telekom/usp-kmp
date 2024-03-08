package de.telekom.usp.mtp.mqtt.kmqtt

import MQTTClient
import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.mqtt.ClientId
import de.telekom.usp.mtp.mqtt.MqttClient
import de.telekom.usp.mtp.mqtt.MqttClientCallback
import de.telekom.usp.mtp.mqtt.QoS
import de.telekom.usp.mtp.mqtt.Subscription
import de.telekom.usp.mtp.mqtt.Topic
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import mqtt.MQTTVersion
import mqtt.packets.mqtt.MQTTConnack
import mqtt.packets.mqtt.MQTTDisconnect
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.MQTT5Properties
import mqtt.packets.mqttv5.SubscriptionOptions
import okio.ByteString
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
        clientId = clientId.value,
        properties = connectProperties,
        onConnected = ::onConnected,
        onDisconnected = ::onDisconnected,
        publishReceived = ::publishReceived
    )

    // --- Interface methods -----------------------------------------------------------------------

    override fun callback(callback: MqttClientCallback) {
        TODO("Not yet implemented")
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
        TODO()
    }

    private fun onDisconnected(disconnect: MQTTDisconnect?) {
        TODO()
    }

    private fun publishReceived(publish: MQTTPublish) {
        TODO()
    }
}