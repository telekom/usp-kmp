/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

/**
 * Describes the type of IoT Level Controller or Sensor that the {{object}} instance is
 * representing. {{enum}}
 */
@Generated
public enum class IoTLevelType(
    public val text: String,
) : DataType {
    ACCELERATION("Acceleration"),
    AREA("Area"),
    BATTERY("Battery"),
    BRIGHTNESS("Brightness"),
    CONCENTRATION("Concentration"),
    CONDUCTIVITY("Conductivity"),
    DISTANCE("Distance"),
    ENERGY("Energy"),
    FLOW("Flow"),
    HUMIDITY("Humidity"),
    INTENSITY("Intensity"),
    LUMINANCE("Luminance"),
    METER("Meter"),
    MOTION("Motion"),
    POSITION("Position"),
    POWER("Power"),
    PRESSURE("Pressure"),
    RADIATION("Radiation"),
    SPEED("Speed"),
    TEMPERATURE("Temperature"),
    /**
     * Amount of space that an object or substance occupies
     */
    VOLUME("Volume"),
    WEIGHT("Weight"),
    ;

    public companion object {
        public fun from(text: String): IoTLevelType? = entries.firstOrNull { it.text == text }
    }
}
