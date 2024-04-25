package de.telekom.usp.cli

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.MessageTransferProtocol
import de.telekom.usp.toEndpoint
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
data class MessageTransferConfig(
    val mtp: MessageTransferProtocol,
    val fromEndpointId: String,
    val toEndpointId: String,
    val mqttConfig: MqttConfig? = null,
    val webSocketConfig: WebSocketConfig? = null
) {
    val localEndpoint: EndpointIdentifier
        get() = fromEndpointId.toEndpoint()

    val remoteEndpoint: EndpointIdentifier
        get() = toEndpointId.toEndpoint()
}

@Serializable
data class MqttConfig(
    val host: String,
    val port: Int = 8883,
    val user: String,
    val password: String,
    val useTls: Boolean = true,
)

@Serializable
data class WebSocketConfig(
    val host: String,
    val port: Int = 8883,
    val pingDuration: Duration = 20.seconds
)