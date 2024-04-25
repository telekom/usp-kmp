package de.telekom.usp.cli

import com.github.ajalt.clikt.core.FileNotFound
import com.github.ajalt.clikt.core.InvalidFileFormat
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.MessageTransfer
import de.telekom.usp.mtp.MessageTransferFactory
import de.telekom.usp.mtp.MessageTransferProtocol.MQTT
import de.telekom.usp.mtp.MessageTransferProtocol.WEB_SOCKET
import de.telekom.usp.mtp.mqtt.MqttTransfer
import de.telekom.usp.mtp.mqtt.SimpleNameProvider
import de.telekom.usp.mtp.ws.WebSocketTransfer
import de.telekom.usp.toEndpoint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

class JsonMessageTransferFactory(
    private val configPath: Path,
    private val debugMode: Boolean = false
) :
    MessageTransferFactory {

    val localEndpoint
        get() = config.localEndpoint

    val remoteEndpoint
        get() = config.remoteEndpoint

    private lateinit var config: MessageTransferConfig

    override fun create(): MessageTransfer {
        readConfig()

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

    @OptIn(ExperimentalSerializationApi::class)
    private fun readConfig() {
        if (!FileSystem.SYSTEM.exists(configPath)) {
            throw FileNotFound("Missing configuration file: '$configPath'")
        }

        try {
            FileSystem.SYSTEM.source(configPath).buffer().use { source ->
                config = Json.decodeFromBufferedSource<MessageTransferConfig>(source)
            }
        } catch (ex: Exception) {
            throw InvalidFileFormat(configPath.toString(), "${ex.message}")
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