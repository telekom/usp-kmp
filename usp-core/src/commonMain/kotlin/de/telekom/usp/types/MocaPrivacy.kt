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
 * Represents a type of MoCA Privacy.
 */
@Generated
public enum class MocaPrivacy(
    public val text: String,
    public val code: Int,
) : DataType {
    MOCA_RESERVED_5("mocaReserved5", 0),
    MOCA_RESERVED_4("mocaReserved4", 1),
    MOCA_RESERVED_3("mocaReserved3", 2),
    MOCA_RESERVED_2("mocaReserved2", 3),
    MOCA_RESERVED_1("mocaReserved1", 4),
    MOCA_2_ENHANCED_PRIVACY("moca2EnhancedPrivacy", 5),
    MOCA_20_PRIVACY("moca20Privacy", 6),
    MOCA_1_PRIVACY("moca1Privacy", 7),
    ;

    public companion object {
        public fun from(text: String): MocaPrivacy? = entries.firstOrNull { it.text == text }

        public fun from(code: Int): MocaPrivacy? = entries.firstOrNull { it.code == code }
    }
}
