//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * This data type represents power levels that are normally expressed in dB. Units are in tenths of
 * a dB; for example, 5.1 dB will be represented as 51.
 */
@JvmInline
@Generated
public value class TenthdB(
    public val wrapped: Int,
) : DataType {
    public constructor(text: String) : this(text.toInt())

    override fun toString(): String = wrapped.toString()
}
