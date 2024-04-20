//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

/**
 * Describes the type of Device that the {{object}} instance is representing. {{enum}}
 */
@Generated
public enum class IoTDeviceType(
    public val text: String,
) : DataType {
    ALARM("Alarm"),
    ANTI_THEFT("AntiTheft"),
    BELL("Bell"),
    CLOCK("Clock"),
    DOOR("Door"),
    FAN("Fan"),
    GARAGE_DOOR("GarageDoor"),
    HVAC("HVAC"),
    LIGHT("Light"),
    LOCK("Lock"),
    METER("Meter"),
    MOTOR("Motor"),
    OVEN("Oven"),
    POWER_STRIP("PowerStrip"),
    SENSOR("Sensor"),
    SENSOR_STRIP("SensorStrip"),
    SIREN("Siren"),
    THERMOSTAT("Thermostat"),
    ;

    public companion object {
        public fun from(text: String): IoTDeviceType? = entries.firstOrNull { it.text == text }
    }
}
