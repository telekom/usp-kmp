//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * Universally Unique Identifier. See {{bibref|RFC4122}}.
 */
@JvmInline
@Generated
public value class UUID(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped

    /**
     * Determines whether this `UUID` has a valid format according to the specification.
     */
    public fun isValid(): Boolean = pattern.matches(wrapped)

    private companion object {
        private val pattern: Regex =
            """[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}""".toRegex()
    }
}
