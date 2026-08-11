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
 * IPv4 address prefix.  Can be any IPv4 prefix that is permitted by the ''IPPrefix'' data type.
 */
@JvmInline
@Generated
public value class IPv4Prefix(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped

    /**
     * Determines whether this `IPv4Prefix` has a valid format according to the specification.
     */
    public fun isValid(): Boolean {
        if (wrapped.isEmpty()) return true
        if (pattern1.matches(wrapped)) return true
        return pattern2.matches(wrapped)
    }

    private companion object {
        private val pattern1: Regex = """/(3[0-2]|[012]?[0-9])""".toRegex()

        private val pattern2: Regex =
            """((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])/(3[0-2]|[012]?[0-9])""".toRegex()
    }
}
