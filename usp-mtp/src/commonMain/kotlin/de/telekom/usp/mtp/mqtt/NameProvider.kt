package de.telekom.usp.mtp.mqtt

import de.telekom.usp.EndpointIdentifier

/**
 * Provides the MQTT topics to which the controller should send publishing packets and from where
 * to received packets.
 */
interface NameProvider {

    /**
     * Provides the name of this controller as its endpoint identifier
     */
    val from: EndpointIdentifier

    /**
     * Provides the client ID of this controller, see also [R-MQTT.8](https://usp.technology/specification/index.htm#r-mqtt.8).
     */
    val clientId: String

    /**
     * Provides the topic from where this controller should expect packets, i.e. where it should
     * subscribe to.
     */
    val ownTopic: Topic

    /**
     * Provides the topic to which this controller should send requests to.
     */
    val remoteTopic: Topic
}