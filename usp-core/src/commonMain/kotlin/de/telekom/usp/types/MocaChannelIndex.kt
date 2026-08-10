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
 * Represents the MoCA 2.0 primary or secondary channel, or MoCA 2.5 first, second, third, fourth,
 * or fifth channel.
 */
@Generated
public enum class MocaChannelIndex(
    public val text: String,
    public val code: Int,
) : DataType {
    PRIMARY("primary", 1),
    SECONDARY("secondary", 2),
    M_25_FIRST("m25first", 3),
    M_25_SECOND("m25second", 4),
    M_25_THIRD("m25third", 5),
    M_25_FOURTH("m25fourth", 6),
    M_25_FIFTH("m25fifth", 7),
    ;

    public companion object {
        public fun from(text: String): MocaChannelIndex? = entries.firstOrNull { it.text == text }

        public fun from(code: Int): MocaChannelIndex? = entries.firstOrNull { it.code == code }
    }
}
