//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * A 32-bit statistics parameter, e.g. a byte counter.  This data type SHOULD NOT be used for
 * statistics parameters whose values might become greater than the maximum value that can be
 * represented as an ''unsignedInt'' (i.e. 0xffffffff, referred to below as ''maxval'').
 * ''StatsCounter64'' SHOULD be used for such parameters.  The value ''maxval'' indicates that no data
 * is available for this parameter. In the unlikely event that the actual value of the statistic is
 * ''maxval'', the CPE SHOULD return ''maxval - 1''.  The actual value of the statistic might be
 * greater than ''maxval''. Such values SHOULD wrap around through zero.  The term ''packet'' is to be
 * interpreted as the transmission unit appropriate to the protocol layer in question, e.g. an IP
 * packet or an Ethernet frame.
 */
@JvmInline
@Generated
public value class StatsCounter32(
    public val wrapped: UInt,
) : DataType {
    public constructor(text: String) : this(text.toUInt())

    override fun toString(): String = wrapped.toString()
}
