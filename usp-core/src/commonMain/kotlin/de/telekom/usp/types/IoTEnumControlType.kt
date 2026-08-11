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
 * Describes the type of IoT Enum Controller that the {{object}} instance is representing. {{enum}}
 */
@Generated
public enum class IoTEnumControlType(
    public val text: String,
) : DataType {
    ALARM_MODE("AlarmMode"),
    DOOR_MODE("DoorMode"),
    FAN_MODE("FanMode"),
    LOCK_MODE("LockMode"),
    OPERATING_MODE("OperatingMode"),
    TEMPERATURE_MODE("TemperatureMode"),
    THERMOSTAT_MODE("ThermostatMode"),
    ;

    public companion object {
        public fun from(text: String): IoTEnumControlType? = entries.firstOrNull { it.text == text }
    }
}
