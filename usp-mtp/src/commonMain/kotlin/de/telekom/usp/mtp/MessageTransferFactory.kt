package de.telekom.usp.mtp

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.MessageTransferProtocol.MQTT
import de.telekom.usp.mtp.MessageTransferProtocol.WEB_SOCKET
import de.telekom.usp.mtp.mqtt.MqttTransfer
import de.telekom.usp.mtp.mqtt.SimpleNameProvider
import de.telekom.usp.mtp.ws.WebSocketTransfer
import de.telekom.usp.toEndpoint

class MessageTransferFactory {

    fun create(config: MessageTransferConfig, debugMode: Boolean = false): MessageTransfer {
        return when (config.mtp) {
            WEB_SOCKET -> {
                config.createWebSocket(config.webSocketConfig!!, debugMode)
            }

            MQTT -> {
                config.createMqtt(config.mqttConfig!!)
            }

            else -> throw MessageTransferFactoryException(
                "MTP ${config.mtp} not supported, " +
                        "possible values: $WEB_SOCKET or $MQTT"
            )
        }
    }

    private fun MessageTransferConfig.createMqtt(config: MqttConfig): MessageTransfer {
        val from = fromEndpointId.toEndpoint()

        return MqttTransfer(
            host = config.host,
            port = config.port,
            user = config.user,
            password = config.password,
            useTls = config.useTls,
            nameProvider = SimpleNameProvider(from, toEndpointId.toEndpoint())
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