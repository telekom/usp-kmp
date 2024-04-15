package de.telekom.usp.mtp

import kotlinx.serialization.Serializable

@Serializable
data class MessageTransferConfig(
    var mtp: MessageTransferProtocol,
    var mqttConfig: MqttConfig? = null,
    var webSocketConfig: WebSocketConfig? = null
)

@Serializable
data class MqttConfig(
    var host: String,
    var port: Int = 8883,
    var user: String,
    var password: String,
    var useTls: Boolean = true,
    var fromEndpointId: String,
    var topic: String,
    var replyToTopic: String,
)

@Serializable
data class WebSocketConfig(
    var host: String,
    var port: Int = 8883,
    var fromEndpointId: String,
)