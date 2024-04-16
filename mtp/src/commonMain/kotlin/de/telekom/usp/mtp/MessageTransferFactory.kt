package de.telekom.usp.mtp

import de.telekom.usp.EndpointIdentifier
import socket.tls.TLSClientSettings

class MessageTransferFactory {

    fun create(config: MessageTransferConfig, debugMode: Boolean = false): MessageTransfer {
        return when (config.mtp) {
            MessageTransferProtocol.WEB_SOCKET -> {
                config.createWebSocket(config.webSocketConfig!!, debugMode)
            }

            MessageTransferProtocol.MQTT -> {
                config.createMqtt(config.mqttConfig!!)
            }

            else -> throw MessageTransferFactoryException("MTP ${config.mtp} not supported")
        }
    }

    private fun MessageTransferConfig.createMqtt(config: MqttConfig): MessageTransfer {
        return MqttTransfer(
            host = config.host,
            port = config.port,
            user = config.user,
            password = config.password,
            tls = if (config.useTls) TLSClientSettings() else null,
            from = EndpointIdentifier(fromEndpointId),
            subscribeTopics = mutableListOf(config.topic),
            replyToTopic = config.replyToTopic
        )
    }

    private fun MessageTransferConfig.createWebSocket(
        config: WebSocketConfig,
        debugMode: Boolean = false
    ): MessageTransfer {
        return WebSocketTransfer(
            host = config.host,
            port = config.port,
            from = EndpointIdentifier(fromEndpointId),
            pingDuration = config.pingDuration,
            debugMode = debugMode
        )
    }
}

class MessageTransferFactoryException(message: String) : Exception(message)