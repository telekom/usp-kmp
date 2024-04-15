package de.telekom.usp.mtp.mqtt

import okio.ByteString

/**
 * Generic interface for handling USP connections to a MQTT server.
 *
 * THIS IS FOR FUTURE USE, NOT COMPLETE AND NOT CURRENTLY USED!
 */
interface MqttClient {

    fun setCallback(callback: MqttClientCallback)

    /**
     * Tries to establish a connection to the remote MQTT server. Implementors must request response
     * information in the CONNECT message according to [R-MQTT.12](https://usp.technology/specification/index.htm#r-mqtt.12)
     * and set the from endpoint ID according to [R-MQTT.13](https://usp.technology/specification/index.htm#r-mqtt.13)
     *
     * @see PROPERTY_ENDPOINT_ID
     */
    fun connect()

    fun disconnect()

    fun subscribeTo(topics: List<Subscription>)

    fun unsubscribeFrom(topics: List<Topic>)

    /**
     * Sends a publish request to the specified topic, using the USP content type for the specified
     * payload.
     *
     * @see Message.USP_CONTENT_TYPE
     */
    fun publish(topic: Topic, payload: ByteString, retain: Boolean, qoS: QoS)

    companion object {

        /** R-MQTT.13 the name of the user property to store the endpoint ID of this when connecting. */
        const val PROPERTY_ENDPOINT_ID = "usp-endpoint-id"
    }
}