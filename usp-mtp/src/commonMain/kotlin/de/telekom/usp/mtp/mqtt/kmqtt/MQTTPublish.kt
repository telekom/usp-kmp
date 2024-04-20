package de.telekom.usp.mtp.mqtt.kmqtt

import de.telekom.usp.mtp.mqtt.Message
import de.telekom.usp.mtp.mqtt.MqttVersion
import de.telekom.usp.mtp.mqtt.QoS
import de.telekom.usp.mtp.mqtt.Topic
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.MQTT5Publish
import okio.ByteString
import okio.ByteString.Companion.toByteString

@OptIn(ExperimentalUnsignedTypes::class)
fun MQTTPublish.toMessage() = object : Message {

    override val version: MqttVersion
        get() = if (this@toMessage is MQTT5Publish) MqttVersion.Mqtt5 else MqttVersion.Mqtt3_1_1

    override val topic: Topic
        get() = Topic(this@toMessage.topicName)

    override val responseTopic: Topic
        get() = if (this@toMessage is MQTT5Publish) {
            // R-MQTT.23 - USP Endpoints using MQTT 5.0 MUST include their “reply to” Topic in the PUBLISH Response Topic property.
            Topic(properties.responseTopic ?: "")
        } else {
            // R-MQTT.24 - USP Endpoints using MQTT 3.1.1 MUST include their “reply to” Topic after “/reply-to=” at the end of the PUBLISH Topic Name
            Topic(topicName.substringAfter("/reply-to=", ""))
        }

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