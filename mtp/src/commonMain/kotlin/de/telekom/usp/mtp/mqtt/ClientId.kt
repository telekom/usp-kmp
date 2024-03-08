package de.telekom.usp.mtp.mqtt

import de.telekom.usp.EndpointIdentifier
import kotlin.jvm.JvmInline

@JvmInline
value class ClientId private constructor(val value: String) {

    companion object {

        private val EMPTY = ClientId("")

        /**
         * Construct a valid client ID according to
         * [R-MQTT.8](https://usp.technology/specification/index.htm#r-mqtt.8)
         */
        fun from(clientId: String?, from: EndpointIdentifier, mqttVersion: MqttVersion): ClientId {
            return if (clientId.isNullOrBlank()) {
                if (mqttVersion == MqttVersion.Mqtt3_1_1) {
                    ClientId(from.toShortString())
                } else {
                    EMPTY
                }
            } else {
                ClientId(clientId)
            }
        }
    }
}