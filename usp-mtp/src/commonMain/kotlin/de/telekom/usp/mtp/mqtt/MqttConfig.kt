/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.mtp.mqtt

/**
 * Provides additional MQTT configuration data beyond connectivity.
 *
 * @property clientId the client ID of this controller or `null` to use some default
 * @property ownTopic the topic from where this controller should expect packets, i.e. where it should subscribe to
 * @property remoteTopic the topic to which this controller should send requests to
 * @property version the version to use for this MQTT connection
 * @property qoS the quality of service to use for this MQTT connection
 */
data class MqttConfig(
    val clientId: String?,
    val ownTopic: Topic,
    val remoteTopic: Topic,
    val version: Version = Version.Mqtt5,
    val qos: QoS = QoS.AT_LEAST_ONCE
) {

    /**
     * Returns the [remoteTopic] with an optionally appended `/reply-to=...` string, when the MQTT
     * version used by this config is 3.1.1. (see R-MQTT.24)
     */
    val fixedRemoteTopic: Topic
        get() = if (version == Version.Mqtt3_1_1) {
            Topic("$remoteTopic/reply-to=${ownTopic.value.replace("/", "%2F")}")
        } else {
            remoteTopic
        }
}