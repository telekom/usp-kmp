//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * Position of the {{object}} entry in the order of precedence. A value of ''1'' indicates the first
 * entry to be considered (highest precedence).  When a {{object}} instance is created, or when an
 * existing {{param}} value is modified, if the value matches that of an existing entry, the {{param}}
 * values for the existing entry and all lower {{param}} entries are incremented (lowered in
 * precedence) to ensure uniqueness of this value. A deletion causes {{param}} values to be compacted.
 * When a value is changed, incrementing occurs before compaction.  If no {{param}} value is supplied
 * on creation of a {{object}} instance, it MUST be assigned a value that is one more than the largest
 * current value (lowest precedence).
 */
@JvmInline
@Generated
public value class Order(
    public val wrapped: UInt,
) : DataType {
    public constructor(text: String) : this(text.toUInt())

    override fun toString(): String = wrapped.toString()
}
