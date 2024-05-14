package de.telekom.usp.mtp.mqtt.kmqtt

import de.telekom.usp.mtp.mqtt.MqttTransfer
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.MQTT5Publish

fun MQTTPublish.replyTo(): String {
    return if (this is MQTT5Publish) {
        // R-MQTT.23 - USP Endpoints using MQTT 5.0 MUST include their “reply to” Topic in the PUBLISH Response Topic property.
        properties.responseTopic ?: ""
    } else {
        // R-MQTT.24 - USP Endpoints using MQTT 3.1.1 MUST include their “reply to” Topic after “/reply-to=” at the end of the PUBLISH Topic Name
        topicName.substringAfter("/reply-to=", "").replace("%2F", "/")
    }
}

val MQTTPublish.contentType: String?
    get() = if (this is MQTT5Publish) {
        properties.contentType
    } else {
        null
    }

fun MQTTPublish.isUspContentType(): Boolean {
    val localContentType = contentType
    return localContentType == null
            || localContentType == MqttTransfer.USP_CONTENT_TYPE
            || localContentType == MqttTransfer.USP_MIME_TYPE_1
            || localContentType == MqttTransfer.USP_MIME_TYPE_2
}

