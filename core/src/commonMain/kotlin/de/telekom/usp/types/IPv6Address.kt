//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * IPv6 address.  Can be any IPv6 address that is permitted by the ''IPAddress'' data type.
 */
@JvmInline
@Generated
public value class IPv6Address(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped
}
