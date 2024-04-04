//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * This data type represents power levels that are normally expressed in dBmV. Units are in tenths
 * of a dBmV; for example, 5.1 dBmV will be represented as 51.
 */
@JvmInline
@Generated
public value class TenthdBmV(
    public val wrapped: Int,
) : DataType {
    public constructor(text: String) : this(text.toInt())

    override fun toString(): String = wrapped.toString()
}
