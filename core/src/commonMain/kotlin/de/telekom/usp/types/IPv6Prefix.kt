//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * IPv6 address prefix.  Can be any IPv6 prefix that is permitted by the ''IPPrefix'' data type.
 */
@JvmInline
@Generated
public value class IPv6Prefix(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped
}
