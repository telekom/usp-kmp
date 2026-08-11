/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.mtp.mqtt.kmqtt

import de.telekom.usp.mtp.mqtt.Version
import mqtt.MQTTVersion

fun Version.toMQTTVersion(): MQTTVersion {
    return when (this) {
        Version.Mqtt5 -> MQTTVersion.MQTT5
        Version.Mqtt3_1_1 -> MQTTVersion.MQTT3_1_1
    }
}