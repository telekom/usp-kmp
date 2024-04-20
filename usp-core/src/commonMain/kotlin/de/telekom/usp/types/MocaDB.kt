//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * Represents one decibel or 1 dB.
 */
@JvmInline
@Generated
public value class MocaDB(
    public val wrapped: UInt,
) : DataType {
    public constructor(text: String) : this(text.toUInt())

    override fun toString(): String = wrapped.toString()
}
