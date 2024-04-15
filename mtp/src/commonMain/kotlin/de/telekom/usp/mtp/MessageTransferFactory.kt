package de.telekom.usp.mtp

import de.telekom.usp.EndpointIdentifier
import socket.tls.TLSClientSettings

class MessageTransferFactory {

    fun create(config: MessageTransferConfig): MessageTransfer {
        return when (config.mtp) {
            MessageTransferProtocol.WEB_SOCKET -> createWebSocket(config.webSocketConfig!!)
            MessageTransferProtocol.MQTT -> createMqtt(config.mqttConfig!!)
            else -> throw MessageTransferFactoryException("MTP ${config.mtp} not supported")
        }
    }

    private fun createMqtt(config: MqttConfig): MessageTransfer {
        return MqttTransfer(
            host = config.host,
            port = config.port,
            user = config.user,
            password = config.password,
            tls = if (config.useTls) TLSClientSettings() else null,
            from = EndpointIdentifier(config.fromEndpointId),
            subscribeTopics = mutableListOf(config.topic),
            replyToTopic = config.replyToTopic
        )
    }

    private fun createWebSocket(config: WebSocketConfig): MessageTransfer {
        return WebSocketTransfer(
            host = config.host,
            port = config.port,
            from = EndpointIdentifier(config.fromEndpointId)
        )
    }
}

class MessageTransferFactoryException(message: String) : Exception(message)