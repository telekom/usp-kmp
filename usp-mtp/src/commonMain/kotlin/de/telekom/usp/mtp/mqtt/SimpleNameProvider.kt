package de.telekom.usp.mtp.mqtt

import de.telekom.usp.EndpointIdentifier

/**
 * Implementation of [NameProvider]. For the topics, this class prepends "usp/controllers/" to the
 * local endpoint ID to provide the own topic and "usp/agents/" to the remote endpoint ID to provide
 * the remote topic.
 */
class SimpleNameProvider(
    private val localEndpoint: EndpointIdentifier,
    private val remoteEndpoint: EndpointIdentifier,
    private val clientIdBase: String? = null,
    private val version: Version = Version.Mqtt5
) : NameProvider {

    override val from: EndpointIdentifier
        get() = localEndpoint

    override val clientId: String
        get() {
            // See R-MQTT.8
            return if (clientIdBase.isNullOrBlank()) {
                if (version == Version.Mqtt3_1_1) {
                    from.toShortString()
                } else {
                    ""
                }
            } else {
                clientIdBase
            }
        }

    override val ownTopic: Topic
        get() = Topic("usp/controllers/${localEndpoint.toShortString()}")

    override val remoteTopic: Topic
        get() {
            return when (version) {
                Version.Mqtt5 -> {
                    Topic("usp/agents/${remoteEndpoint.toShortString()}")
                }

                Version.Mqtt3_1_1 -> {
                    // R-MQTT.24: append reply-to topic name for MQTT v3
                    Topic(
                        "usp/agents/${remoteEndpoint.toShortString()}/reply-to=${
                            ownTopic.value.replace("/", "%2F")
                        }"
                    )
                }
            }
        }
}