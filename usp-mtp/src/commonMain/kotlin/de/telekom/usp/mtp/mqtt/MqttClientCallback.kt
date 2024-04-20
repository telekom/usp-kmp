package de.telekom.usp.mtp.mqtt

interface MqttClientCallback {

    fun onConnected(connack: Connack)

    fun onDisconnected()

    fun onMessage(message: Message)
}