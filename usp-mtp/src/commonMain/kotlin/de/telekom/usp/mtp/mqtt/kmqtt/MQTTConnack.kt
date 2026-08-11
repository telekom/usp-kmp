/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.mtp.mqtt.kmqtt

import de.telekom.usp.mtp.mqtt.Topic
import mqtt.packets.mqtt.MQTTConnack
import mqtt.packets.mqttv5.MQTT5Connack


fun MQTTConnack.subscriptionTopics(): List<Topic> {
    // R-MQTT.15
    return if (this is MQTT5Connack) {
        properties.userProperty.filter { it.first == "subscribe-topic" }.map { Topic(it.second) }
    } else {
        emptyList()
    }
}