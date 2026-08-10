/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * The IEEE EUI 64-bit identifier as defined in {{bibref|IEEE_EUI64}}. The IEEE defined 64-bit
 * extended unique identifier (EUI-64) is a concatenation of:  * The 24-bit (OUI-24) or 36-bit (OUI-36)
 * company_id value assigned by the   IEEE Registration Authority (IEEE-RA), and  * The extension
 * identifier (40 bits for OUI-24 or 28 bits for OUI-36)   assigned by the organization with that
 * company_id assignment.
 */
@JvmInline
@Generated
public value class IEEE_EUI64(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped

    /**
     * Determines whether this `IEEE_EUI64` has a valid format according to the specification.
     */
    public fun isValid(): Boolean {
        if (wrapped.isEmpty()) return true
        return pattern.matches(wrapped)
    }

    private companion object {
        private val pattern: Regex =
            """([0-9A-Fa-f][0-9A-Fa-f]:){7}([0-9A-Fa-f][0-9A-Fa-f])""".toRegex()
    }
}
