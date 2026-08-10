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

public fun PSDBreakPointIndexAndLevel(text: String): PSDBreakPointIndexAndLevel {
    val (value1, value2) = text.split(",").map { it.trim().toInt() }
    return PSDBreakPointIndexAndLevel(packInts(value1, value2))
}

/**
 * # the Power Spectral Density (PSD) breakpoint sub-carrier index in the   range [0:49152] with Df
 * = 4.3125 kHz frequency spacing, and  # the value of the level of the PSD at this sub-carrier
 * expressed in   ''0.1 dBm/Hz'' with an offset of -200 dBm/Hz. The range of valid values   for PSD
 * is -30 to -200 dBm/Hz.  Both values are represented as unsignedInt.
 */
@JvmInline
@Generated
public value class PSDBreakPointIndexAndLevel internal constructor(
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
