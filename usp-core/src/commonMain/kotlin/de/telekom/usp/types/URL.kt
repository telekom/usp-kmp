//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * Uniform Resource Locator. See {{bibref|RFC3986}} (URI), {{bibref|IANA-uri-schemes}}, and
 * individual URI scheme RFCs such as {{bibref|RFC7252}} (''coap'', ''coaps'') and {{bibref|RFC7230}}
 * (''http'', ''https'').
 */
@JvmInline
@Generated
public value class URL(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped
}
