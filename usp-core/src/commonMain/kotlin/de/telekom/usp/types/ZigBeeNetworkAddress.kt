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
 * The ZigBee 16-bit network address (NWK) as defined in {{bibref|ZigBee2007}}. The address is
 * assigned to a device by the network layer and used by the network layer for routing messages between
 * devices.
 */
@JvmInline
@Generated
public value class ZigBeeNetworkAddress(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped

    /**
     * Determines whether this `ZigBeeNetworkAddress` has a valid format according to the
     * specification.
     */
    public fun isValid(): Boolean {
        if (wrapped.isEmpty()) return true
        return pattern.matches(wrapped)
    }

    private companion object {
        private val pattern: Regex = """([0-9A-Fa-f]){4}""".toRegex()
    }
}
