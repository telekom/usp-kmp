package de.telekom.usp.mtp

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class MessageTransferConfigTest {

    @Test
    @OptIn(ExperimentalSerializationApi::class)
    fun test() {
        val config = MessageTransferConfig(
            mtp = MessageTransferProtocol.MQTT,
            fromEndpointId = "proto::usp-demo",
            toEndpointId = "proto::AXACT",
            webSocketConfig = WebSocketConfig(
                host = "localhost",
                port = 443,
            ),
            mqttConfig = MqttConfig(
                host = "localhost",
                port = 8883,
                user = "demo-user",
                password = "secret",
                useTls = true,
                topic = "usp-demo-topic",
                replyToTopic = "reply-to"
            )
        )
        val json = Json {
            prettyPrint = true
            prettyPrintIndent = "    "
        }
        val text = json.encodeToString(config)
        println(text)
        val config1 = json.decodeFromString<MessageTransferConfig>(text)
        println(config1)
    }
}