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
 * All MAC addresses are represented as strings of 12 hexadecimal digits (digits 0-9, letters A-F or
 * a-f) displayed as six pairs of digits separated by colons. Unspecified or inapplicable MAC addresses
 * MUST be represented as empty strings unless otherwise specified by the parameter definition.
 */
@JvmInline
@Generated
public value class MACAddress(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped

    /**
     * Determines whether this `MACAddress` has a valid format according to the specification.
     */
    public fun isValid(): Boolean {
        if (wrapped.isEmpty()) return true
        return pattern.matches(wrapped)
    }

    private companion object {
        private val pattern: Regex =
            """([0-9A-Fa-f][0-9A-Fa-f]:){5}([0-9A-Fa-f][0-9A-Fa-f])""".toRegex()
    }
}
