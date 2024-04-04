//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

public fun SELTPAttenuationCharacteristicsIndexAndTFlog(text: String):
        SELTPAttenuationCharacteristicsIndexAndTFlog {
    val (value1, value2) = text.split(",").map { it.trim().toInt() }
    return SELTPAttenuationCharacteristicsIndexAndTFlog(packInts(value1, value2))
}

/**
 * # The paired frequency spacing index in the range [0:8191], and  # The transfer function log
 * value, i.e. [i, TFlog(i *   {{param|TFlogGroupSize}} * Df)], where the reference frequency spacing
 * Df = 4.3125 kHz, the index i valid range is 0 to 8191, and TFlog(i *   {{param|TFlogGroupSize}} *
 * Df) spans a range from +6.0 dB down to -96.2   dB with units of 0.1 dB.  Both values are represented
 * as unsignedInt.
 */
@JvmInline
@Generated
public value class SELTPAttenuationCharacteristicsIndexAndTFlog internal constructor(
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
