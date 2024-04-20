package de.telekom.usp.mtp.mqtt.kmqtt

import de.telekom.usp.mtp.mqtt.Connack
import de.telekom.usp.mtp.mqtt.Topic
import mqtt.packets.mqtt.MQTTConnack
import mqtt.packets.mqttv5.MQTT5Connack

fun MQTTConnack.toConnack() = object : Connack {

    override val subscriptionTopics: List<Topic>
        get() = if (this@toConnack is MQTT5Connack) {
            this@toConnack.properties.userProperty
                .filter { it.first == Connack.PROPERTY_SUBSCRIBE_TOPIC }
                .map { Topic(it.second) }
        } else {
            emptyList()
        }

    override val responseInformation: Topic?
        get() = if (this@toConnack is MQTT5Connack) {
            this@toConnack.properties.responseInformation?.let { Topic(it) }
        } else {
            null
        }
}