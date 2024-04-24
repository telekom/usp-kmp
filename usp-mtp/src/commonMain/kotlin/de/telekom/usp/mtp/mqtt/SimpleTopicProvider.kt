package de.telekom.usp.mtp.mqtt

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.MqttTopicProvider

/**
 * Implementation of [MqttTopicProvider] simply prepending "usp/controllers/" to the local endpoint
 * ID to provide the own topic and "usp/agents/" to the remote endpoint ID to provide the remote
 * topic.
 */
class SimpleTopicProvider(
    private val localEndpoint: EndpointIdentifier,
    private val remoteEndpoint: EndpointIdentifier,
    private val version: MqttVersion = MqttVersion.Mqtt5
) : MqttTopicProvider {

    override val ownTopic: String
        get() = "usp/controllers/${localEndpoint.toShortString()}"

    override val remoteTopic: String
        get() {
            return when (version) {
                MqttVersion.Mqtt5 -> {
                    "usp/agents/${remoteEndpoint.toShortString()}"
                }

                MqttVersion.Mqtt3_1_1 -> {
                    // R-MQTT.24: append reply-to topic name for MQTT v3
                    "usp/agents/${remoteEndpoint.toShortString()}/reply-to=${
                        ownTopic.replace("/", "%2F")
                    }"
                }
            }
        }
}