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
 * Represents the MAC throughput in Kbps.
 */
@JvmInline
@Generated
public value class MocaMacRate(
    public val wrapped: UInt,
) : DataType {
    public constructor(text: String) : this(text.toUInt())

    override fun toString(): String = wrapped.toString()
}
