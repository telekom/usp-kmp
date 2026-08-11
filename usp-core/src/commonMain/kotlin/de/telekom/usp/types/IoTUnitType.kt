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
 * Possible Unit types used for decimal values. {{enum}}
 */
@Generated
public enum class IoTUnitType(
    public val text: String,
) : DataType {
    /**
     * Dimensionless quantity
     */
    DIMENSIONLESS("-"),
    /**
     * Percent
     */
    PERCENT("%"),
    /**
     * Decimal degrees
     */
    DEG("deg"),
    /**
     * Celsius
     */
    DEG_C("degC"),
    /**
     * Fahrenheit
     */
    DEG_F("degF"),
    /**
     * Kelvin [SI]
     */
    KELVIN("K"),
    /**
     * Kilometer [SI]
     */
    KM("km"),
    /**
     * Meter [SI]
     */
    METER("m"),
    /**
     * Centimeter [SI]
     */
    CM("cm"),
    /**
     * Millimeter [SI]
     */
    MM("mm"),
    /**
     * Hour
     */
    HOUR("h"),
    /**
     * Minute
     */
    MIN("min"),
    /**
     * Second [SI]
     */
    SECOND("s"),
    /**
     * Millisecond
     */
    MS("ms"),
    /**
     * Square kilometer
     */
    SQ_KM("sq-km"),
    /**
     * Square meter
     */
    SQ_M("sq-m"),
    /**
     * Square cm
     */
    SQ_CM("sq-cm"),
    /**
     * Cubic meter
     */
    CU_M("cu-m"),
    /**
     * Liter [SI]
     */
    LITER("l"),
    /**
     * Centiliter [SI]
     */
    CL("cl"),
    /**
     * Milliliter [SI]
     */
    ML("ml"),
    /**
     * Kilogram [SI]
     */
    KG("kg"),
    /**
     * Gram [SI]
     */
    GRAM("g"),
    /**
     * Milligram [SI]
     */
    MG("mg"),
    /**
     * Watt hour
     */
    WH("Wh"),
    /**
     * Kilowatt hour
     */
    K_WH("kWh"),
    /**
     * Watt [SI]
     */
    WATT("W"),
    /**
     * Ampere [SI]
     */
    AMPERE("A"),
    /**
     * Hertz [SI]
     */
    HZ("Hz"),
    /**
     * Volt [SI]
     */
    VOLT("V"),
    /**
     * Newton [SI]
     */
    NEWTON("N"),
    /**
     * Pascal [SI]
     */
    PA("Pa"),
    /**
     * Coulomb [SI]
     */
    COULOMB("C"),
    /**
     * Farad [SI]
     */
    FARAD("F"),
    /**
     * Ohm [SI]
     */
    OHM("ohm"),
    /**
     * Siemens [SI]
     */
    SIEMENS("S"),
    /**
     * Weber [SI]
     */
    WB("Wb"),
    /**
     * Tesla [SI]
     */
    TESLA("T"),
    /**
     * Henry [SI]
     */
    HENRY("H"),
    /**
     * Lumen [SI]
     */
    LM("lm"),
    /**
     * Lux [SI]
     */
    LX("lx"),
    /**
     * Meter per second
     */
    MPS("mps"),
    /**
     * Candela [SI]
     */
    CD("cd"),
    /**
     * Mole [SI]
     */
    MOL("mol"),
    /**
     * Ultraviolet index
     */
    UV("UV"),
    /**
     * RGB color, encoded as integer value between 0 (usually represented as 0x000000) and 16777215
     * (usually represented as 0xFFFFFF), e.g. Blue would be 255 (usually represented as 0x0000FF)
     */
    RGB("RGB"),
    /**
     * Parts per million (Alternative use percent: 1ppm = 0.0001%)
     */
    PPM("ppm"),
    /**
     * Sievert (J/kg) [SI]
     */
    SV("Sv"),
    /**
     * Joule [SI]
     */
    JOULE("J"),
    ;

    public companion object {
        public fun from(text: String): IoTUnitType? = entries.firstOrNull { it.text == text }
    }
}
