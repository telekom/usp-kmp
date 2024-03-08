package de.telekom.usp.mtp.mqtt

import okio.ByteString

interface MqttClient {

    fun callback(callback: MqttClientCallback)

    fun subscribeTo(topics: List<Subscription>)

    fun unsubscribeFrom(topics: List<Topic>)

    fun publish(topic: Topic, payload: ByteString, retain: Boolean, qoS: QoS)
}