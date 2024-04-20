//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

public fun PSMBreakPointIndexAndLevel(text: String): PSMBreakPointIndexAndLevel {
    val (value1, value2) = text.split(",").map { it.trim().toInt() }
    return PSMBreakPointIndexAndLevel(packInts(value1, value2))
}

/**
 * # The PSM breakpoint sub-carrier index in the range [0:4095], and  # the value of the level of
 * the PSM at this sub-carrier expressed in   ''0.1 dBm/Hz'' with an offset of -140 dBm/Hz.  Both
 * values are represented as unsignedInt.  {{bibref|G.9964|Clause 5.2}} defines limits on PSM
 * breakpoint levels.
 */
@JvmInline
@Generated
public value class PSMBreakPointIndexAndLevel internal constructor(
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
