/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.mtp.mqtt.kmqtt

import de.telekom.usp.mtp.mqtt.QoS
import mqtt.packets.Qos

fun QoS.toQos() = Qos.valueOf(this.value)!!