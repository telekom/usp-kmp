package de.telekom.usp.mtp

import MQTTClient
import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mqtt.MQTTVersion
import mqtt.Subscription
import mqtt.packets.Qos
import mqtt.packets.mqtt.MQTTConnack
import mqtt.packets.mqtt.MQTTDisconnect
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.ReasonCode
import mqtt.packets.mqttv5.SubscriptionOptions
import okio.ByteString
import okio.ByteString.Companion.toByteString

@OptIn(ExperimentalUnsignedTypes::class)
class MqttTransfer(
    private val host: String,
    private val port: Int,
    private val from: EndpointIdentifier,
    private val topic: String,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : MessageTransfer {

    private val _events = MutableSharedFlow<MessageTransferEvent>()
    override val events: SharedFlow<MessageTransferEvent>
        get() = _events.asSharedFlow()

    private val onConnected: (connack: MQTTConnack) -> Unit = { connack ->
        Logger.d { "Mqtt transfer connected, session present: ${connack.connectAcknowledgeFlags.sessionPresentFlag}" }
        emit(MessageTransferEvent.Connected(to = this@MqttTransfer))
    }

    private val onDisconnected: (disconnect: MQTTDisconnect?) -> Unit = { diconnect ->
        Logger.d { "Mqtt transfer disconnected: $diconnect" }
        emit(MessageTransferEvent.Disconnected(from = this@MqttTransfer))
    }

    private val publishReceived: (publish: MQTTPublish) -> Unit = { publish ->
        publish.payload?.toByteArray()?.toByteString()?.let {
            emit(MessageTransferEvent.BytesReceived(it))
        }
    }

    private val client = MQTTClient(
        mqttVersion = MQTTVersion.MQTT5,
        address = host,
        port = port,
        tls = null,
        clientId = from.toShortString(),
        onConnected = onConnected,
        onDisconnected = onDisconnected,
        publishReceived = publishReceived
    )

    private var clientJob: Job? = null

    init {
        client.subscribe(listOf(Subscription(topic, SubscriptionOptions(Qos.EXACTLY_ONCE))))
    }

    override suspend fun send(bytes: ByteString) {
        TODO("Not yet implemented")
    }

    override suspend fun connect() {
        clientJob = scope.launch {
            try {
                while (client.running) {
                    client.step()
                    delay(100)
                }
            } catch (ex: Exception) {
                Logger.d(throwable = ex) { "Mqtt client no longer connected" }
            }

            emit(MessageTransferEvent.Disconnected(from = this@MqttTransfer))
        }
    }

    override suspend fun disconnect() {
        client.disconnect(ReasonCode.SUCCESS)
        clientJob?.join()
    }

    private fun emit(event: MessageTransferEvent) {
        scope.launch {
            _events.emit(event)
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
        println("Connected")
        delay(200000)
        println("Disconnecting...")
        transfer.disconnect()
        println("Disconnected")
        delay(1000)
    }
}