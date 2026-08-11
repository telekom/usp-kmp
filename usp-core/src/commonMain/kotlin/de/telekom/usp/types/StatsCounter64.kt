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
 * A 64-bit statistics parameter, e.g. a byte counter.  This data type SHOULD be used for all
 * statistics parameters whose values might become greater than the maximum value that can be
 * represented as an ''unsignedInt''.  The maximum value that can be represented as an ''unsignedLong''
 * (i.e. 0xffffffffffffffff) indicates that no data is available for this parameter.  The term
 * ''packet'' is to be interpreted as the transmission unit appropriate to the protocol layer in
 * question, e.g. an IP packet or an Ethernet frame.
 */
@JvmInline
@Generated
public value class StatsCounter64(
    public val wrapped: ULong,
) : DataType {
    public constructor(text: String) : this(text.toULong())

    override fun toString(): String = wrapped.toString()
}
