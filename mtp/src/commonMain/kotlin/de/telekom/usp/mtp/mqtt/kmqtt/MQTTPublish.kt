package de.telekom.usp.mtp.mqtt.kmqtt

import de.telekom.usp.mtp.mqtt.Message
import de.telekom.usp.mtp.mqtt.QoS
import de.telekom.usp.mtp.mqtt.Topic
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.MQTT5Publish
import okio.ByteString
import okio.ByteString.Companion.toByteString

@OptIn(ExperimentalUnsignedTypes::class)
fun MQTTPublish.toMessage() = object : Message {

    override val topic: Topic
        get() = Topic(this@toMessage.topicName)

    override val contentType: String?
        get() = if (this@toMessage is MQTT5Publish) {
            properties.contentType
        } else {
            null
        }

    override val payload: ByteString
        get() = this@toMessage.payload?.toByteArray()?.toByteString() ?: ByteString.EMPTY

    override val qos: QoS
        get() = QoS.valueOf(this@toMessage.qos.value)!!
}