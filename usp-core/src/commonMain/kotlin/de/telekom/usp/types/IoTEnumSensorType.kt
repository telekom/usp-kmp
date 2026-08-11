/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

/**
 * Describes the type of IoT Enum Sensor that the {{object}} instance is representing. {{enum}}
 */
@Generated
public enum class IoTEnumSensorType(
    public val text: String,
) : DataType {
    ALARM_STATE("AlarmState"),
    DOOR_STATE("DoorState"),
    FAN_STATE("FanState"),
    LOCK_STATE("LockState"),
    OPERATING_STATE("OperatingState"),
    TEMPERATURE_STATE("TemperatureState"),
    THERMOSTAT_FAN_STATE("ThermostatFanState"),
    ;

    public companion object {
        public fun from(text: String): IoTEnumSensorType? = entries.firstOrNull { it.text == text }
    }
}
