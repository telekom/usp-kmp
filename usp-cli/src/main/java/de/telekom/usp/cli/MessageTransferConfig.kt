/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

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
    val mqttConnection: MqttConnection? = null,
    val webSocketConnection: WebSocketConnection? = null
) {
    val from: EndpointIdentifier
        get() = fromEndpointId.toEndpoint()

    val to: EndpointIdentifier
        get() = toEndpointId.toEndpoint()

    fun whenMqtt(block: (MqttConnection) -> Unit) {
        if (mtp == MessageTransferProtocol.MQTT) {
            checkNotNull(mqttConnection) { "MTP is set to MQTT, but the MQTT connection settings are missing" }
            block(mqttConnection)
        }
    }

    fun whenWebsocket(block: (WebSocketConnection) -> Unit) {
        if (mtp == MessageTransferProtocol.WEB_SOCKET) {
            checkNotNull(webSocketConnection) { "MTP is set to Websocket, but the WS connection settings are missing" }
            block(webSocketConnection)
        }
    }
}

@Serializable
data class MqttConnection(
    val host: String,
    val port: Int = 8883,
    val user: String,
    val password: String,
    val useTls: Boolean = true,
)

@Serializable
data class WebSocketConnection(
    val host: String,
    val port: Int = 8883,
    val pingDuration: Duration = 20.seconds
)