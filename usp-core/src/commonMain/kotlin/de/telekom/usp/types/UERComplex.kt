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

public fun UERComplex(text: String): UERComplex {
    val (value1, value2) = text.split(",").map { it.trim().toInt() }
    return UERComplex(packInts(value1, value2))
}

/**
 * Pair of 32-bit signed integers a(i),b(i) with each pair representing a complex component of the
 * uncalibrated echo response (UER);  # Real UER component, a(i)  # Imaginary UER component, b(i)  for
 * values of i starting at i=0. Both values are represented as signed integers.  The interpretation of
 * the UER value is as defined in {{bibref|G.996.2|Clause A.2.2.1}}.
 */
@JvmInline
@Generated
public value class UERComplex internal constructor(
    public val wrapped: Long,
) : DataType {
    public val value1: Int
        get() = unpackInt1(wrapped)

    public val value2: Int
        get() = unpackInt2(wrapped)

    public constructor(value1: Int, value2: Int) : this(packInts(value1, value2))

    public operator fun component1(): Int = value1

    public operator fun component2(): Int = value2

    override fun toString(): String = "$value1,$value2"
}
