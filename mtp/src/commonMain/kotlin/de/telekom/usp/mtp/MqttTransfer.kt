package de.telekom.usp.mtp

import MQTTClient
import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mqtt.MQTTVersion
import mqtt.Subscription
import mqtt.packets.Qos
import mqtt.packets.mqtt.MQTTConnack
import mqtt.packets.mqtt.MQTTDisconnect
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.MQTT5Properties
import mqtt.packets.mqttv5.ReasonCode
import mqtt.packets.mqttv5.SubscriptionOptions
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import socket.SocketClosedException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalUnsignedTypes::class)
class MqttTransfer(
    private val host: String,
    private val port: Int,
    private val from: EndpointIdentifier,
    private val topic: String,
    private val mqttVersion: MQTTVersion = MQTTVersion.MQTT5,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : AbstractMessageTransfer() {

    // Unfortunately the MQTT client library uses polling to retrieve published messages, hence we
    // need to run it in a polling loop.
    private val pollingInterval = 50.milliseconds

    private val connectProperties = MQTT5Properties()

    private val publishingProperties = MQTT5Properties()

    private val onConnected: (connack: MQTTConnack) -> Unit = { connack ->
        Logger.d { "Mqtt transfer connected, session present: ${connack.connectAcknowledgeFlags.sessionPresentFlag}" }
        launchEmit(MessageTransferEvent.Connected(to = this@MqttTransfer))
    }

    private val onDisconnected: (disconnect: MQTTDisconnect?) -> Unit = { diconnect ->
        Logger.d { "Mqtt transfer disconnected: $diconnect" }
        launchEmit(MessageTransferEvent.Disconnected(from = this@MqttTransfer))
    }

    private val publishReceived: (publish: MQTTPublish) -> Unit = { publish ->
        publish.payload?.toByteArray()?.toByteString()?.let {
            launchEmit(MessageTransferEvent.BytesReceived(it))
        }
    }

    private val client = MQTTClient(
        mqttVersion = mqttVersion,
        address = host,
        port = port,
        tls = null,
        clientId = from.toShortString(),
        properties = connectProperties,
        onConnected = onConnected,
        onDisconnected = onDisconnected,
        publishReceived = publishReceived
    )

    private var receiverJob: Job? = null
    private var senderJob: Job? = null

    init {
        // R-MQTT.12: An MQTT 5.0 USP Endpoint MUST support setting the Request Response Information property to 1
        publishingProperties.requestResponseInformation = 1u

        // R-MQTT.13: An MQTT 5.0 USP Endpoint MUST include a User Property name-value pair in the CONNECT packet with name of “usp-endpoint-id”
        connectProperties.addUserProperty("usp-endpoint-id" to from.toShortString())
    }

    override suspend fun connect() {
        if (!isConnected()) {
            receiverJob = scope.launch {
                try {
                    // This will open the socket connection to the MQTT server:
                    client.subscribe(
                        listOf(
                            Subscription(
                                topic,
                                SubscriptionOptions(Qos.EXACTLY_ONCE)
                            )
                        )
                    )

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
                                false,
                                Qos.EXACTLY_ONCE,
                                topic,
                                payload,
                                publishingProperties
                            )
                            Logger.d { "Payload of size ${bytes.size} sent to topic '$topic'" }
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

    override fun toString(): String {
        return "MqttTransfer [from: '$from', topic: '$topic', server: $host:$port]"
    }
}

fun main() {
    val from = EndpointIdentifier("proto::usp-demo")
    val transfer = MqttTransfer(
        host = "home.kempmobil.de",
        port = 1883,
        from = from,
        topic = "usp-demo-topic",
    )
    runBlocking {
        println("Connecting...")
        transfer.connect()
        println("Connected, sending...")
        transfer.send("abc".encodeUtf8())
        delay(2.seconds)
        println("Disconnecting...")
        transfer.disconnect()
        println("Disconnected")
        delay(1.seconds)
    }
}