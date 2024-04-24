package de.telekom.usp.mtp

interface MqttTopicProvider {

    val ownTopic: String

    val remoteTopic: String
}