/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.telekom.usp.e2e

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.messages.MessageConverterImpl
import de.telekom.usp.mtp.MessageTransfer
import de.telekom.usp.mtp.mqtt.*
import de.telekom.usp.mtp.ws.WebSocketTransfer
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Configure and create an instance of [MessageExchange] to send data between the two specified
 * endpoint IDs.
 */
fun e2eMessageExchange(
    from: EndpointIdentifier,
    to: EndpointIdentifier,
    init: MessageExchangeBuilder.() -> Unit
): MessageExchange {
    return MessageExchangeBuilder(from, to).run {
        init()
        build()
    }
}

@DslMarker
annotation class UspDslMarker

@UspDslMarker
class MessageExchangeBuilder(
    private val from: EndpointIdentifier,
    private val to: EndpointIdentifier
) {
    private var mqttBuilder: MqttTransferBuilder? = null

    private var wsBuilder: WebSocketTransferBuilder? = null

    var allowSessionContext: Boolean = false

    var scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var requestTimeout: Duration = 10.seconds

    var debugMode: Boolean = false

    fun mqttTransfer(host: String, port: Int = 8333, init: MqttTransferBuilder.() -> Unit) {
        mqttBuilder = MqttTransferBuilder(host, port, from, to).also { it.init() }
    }

    fun wsTransfer(host: String, port: Int = 443, init: WebSocketTransferBuilder.() -> Unit) {
        wsBuilder = WebSocketTransferBuilder(host, port, from, to).also { it.init() }
    }

    fun build(): MessageExchange {
        val transfer: MessageTransfer = if (mqttBuilder != null && wsBuilder != null) {
            throw IllegalStateException("Both MTPs configured, either configure MQTT or Websocket")
        } else if (mqttBuilder != null) {
            mqttBuilder!!.build(scope)
        } else if (wsBuilder != null) {
            wsBuilder!!.build(scope, debugMode)
        } else {
            throw IllegalStateException("Missing MTP configuration")
        }

        val converter = MessageConverterImpl(
            local = from,
            remote = to,
            allowSessionContext = allowSessionContext
        )

        return MessageExchange(
            converter = converter,
            transfer = transfer,
            scope = scope,
            requestTimeout = requestTimeout
        )
    }
}

@UspDslMarker
class WebSocketTransferBuilder(
    private val host: String,
    private val port: Int,
    private val from: EndpointIdentifier,
    @Suppress("unused") private val to: EndpointIdentifier
) {
    var engine: HttpClientEngine = CIO.create()

    var pingDuration: Duration = 20.seconds

    fun build(scope: CoroutineScope, debugMode: Boolean): WebSocketTransfer {
        return WebSocketTransfer(
            host = host,
            port = port,
            from = from,
            scope = scope,
            engine = engine,
            pingDuration = pingDuration,
            debugMode = debugMode
        )
    }
}

@UspDslMarker
class MqttTransferBuilder(
    private val host: String,
    private val port: Int,
    private val from: EndpointIdentifier,
    private val to: EndpointIdentifier
) {
    private val configBuilder = MqttConfigBuilder(from, to)

    /**
     * The username used for authentication against the MQTT server.
     */
    var user: String? = null

    /**
     * The password used for authentication against the MQTT server.
     */
    var password: String? = null

    /**
     * When `true`, use TLS for the connection.
     */
    var useTls: Boolean = true

    /**
     * The web socket path to enable MQTT via websockets (usually `/mqtt`) or `null` to not use
     * web sockets.
     */
    var webSocketPath: String? = null

    fun configure(init: MqttConfigBuilder.() -> Unit) = configBuilder.init()

    fun build(scope: CoroutineScope): KtorMqttTransfer {
        return KtorMqttTransfer(
            host = host,
            port = port,
            user = user,
            pwd = password,
            useTls = useTls,
            webSocketPath = webSocketPath,
            from = from,
            mqttConfig = configBuilder.build(),
            scope = scope
        )
    }
}

@UspDslMarker
class MqttConfigBuilder(
    private val from: EndpointIdentifier,
    private val to: EndpointIdentifier,
) {
    /**
     * The MQTT ID of this client, see R-MQTT.8 for details on what is used when this is `null`.
     */
    var clientId: String? = null

    /**
     * The name of the topic of this controller. When `null` use "usp/controllers/$fromEndpointId".
     */
    var ownTopic: Topic? = null

    /**
     * The name of the remote topic to subscribe to. When `null` use ""usp/agents/$toEndpointId".
     */
    var remoteTopic: Topic? = null

    /**
     * The QoS value to use, defaults to at least once.
     */
    var qos: QoS = QoS.AT_LEAST_ONCE

    /**
     * The MQTT version to use, defaults to 5.0
     */
    var version: Version = Version.Mqtt5

    fun build(): MqttConfig {
        return MqttConfig(
            clientId = clientId,
            ownTopic = ownTopic ?: Topic("usp/controllers/${from.toShortString()}"),
            remoteTopic = remoteTopic ?: Topic("usp/agents/${to.toShortString()}"),
            version = version,
            qos = qos
        )
    }
}
