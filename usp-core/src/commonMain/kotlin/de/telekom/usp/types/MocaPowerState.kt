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
 * Represents the Power State defined by the MoCA2.0 specification.  {{enum}}  See
 * {{bibref|MoCAv2.0|section 12}}.
 */
@Generated
public enum class MocaPowerState(
    public val text: String,
    public val code: Int,
) : DataType {
    /**
     * Power State M0: Active.
     */
    M_0_ACTIVE("m0Active", 0),
    /**
     * Power State M1: Low Power Idle.
     */
    M_1_LOW_POWER_IDLE("m1LowPowerIdle", 1),
    /**
     * Power State M2: Standby.
     */
    M_2_STANDBY("m2Standby", 2),
    /**
     * Power State M3: Sleep.
     */
    M_3_SLEEP("m3Sleep", 3),
    ;

    public companion object {
        public fun from(text: String): MocaPowerState? = entries.firstOrNull { it.text == text }

        public fun from(code: Int): MocaPowerState? = entries.firstOrNull { it.code == code }
    }
}
