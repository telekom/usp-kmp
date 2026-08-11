/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * IPv4 address (or subnet mask).  Can be any IPv4 address that is permitted by the ''IPAddress''
 * data type.
 */
@JvmInline
@Generated
public value class IPv4Address(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped

    /**
     * Determines whether this `IPv4Address` has a valid format according to the specification.
     */
    public fun isValid(): Boolean {
        if (wrapped.isEmpty()) return true
        return pattern.matches(wrapped)
    }

    private companion object {
        private val pattern: Regex =
            """((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])""".toRegex()
    }
}
