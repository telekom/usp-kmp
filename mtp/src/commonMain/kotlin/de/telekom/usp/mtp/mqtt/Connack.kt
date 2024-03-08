package de.telekom.usp.mtp.mqtt

interface Connack {

    /**
     * Returns a list of "subscribe-topic" names when present in this CONNACK message, otherwise an
     * empty list; see also [R-MQTT.15](https://usp.technology/specification/index.htm#r-mqtt.15).
     *
     * @see PROPERTY_SUBSCRIBE_TOPIC
     */
    val subscriptionTopics: List<Topic>

    /**
     * Returns the value of the response information of this CONNACK message, if any. This is used
     * as the reply-to information when publishing records; see also
     * [R-MQTT.21](https://usp.technology/specification/index.htm#r-mqtt.21).
     */
    val responseInformation: Topic?

    companion object {

        // R-MQTT.15
        const val PROPERTY_SUBSCRIBE_TOPIC = "subscribe-topic"
    }
}