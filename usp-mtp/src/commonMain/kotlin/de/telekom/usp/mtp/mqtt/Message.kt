package de.telekom.usp.mtp.mqtt

import okio.ByteString

interface Message {

    val version: MqttVersion

    val topic: Topic

    val responseTopic: Topic

    val contentType: String?

    val payload: ByteString

    val qos: QoS

    fun isUspContent() =
        version == MqttVersion.Mqtt3_1_1 || contentType == USP_CONTENT_TYPE || contentType == USP_MIME_TYPE

    companion object {

        // R-MQTT.27a
        const val USP_CONTENT_TYPE = "usp.msg"
        const val USP_MIME_TYPE = "application/vnd.bbf.usp.msg"
    }
}