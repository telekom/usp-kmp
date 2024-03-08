package de.telekom.usp.mtp.mqtt

interface MqttClientFactory {

    fun create(): MqttClient
}