package de.telekom.usp.mtp.mqtt.kmqtt

import de.telekom.usp.mtp.mqtt.QoS
import mqtt.packets.Qos

fun QoS.toQos() = Qos.valueOf(this.value)!!