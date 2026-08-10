/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import okio.ByteString
import okio.ByteString.Companion.decodeHex
import kotlin.jvm.JvmInline

/**
 * Represents the subcarrier modulation.  Binary string array (array of two hexadecimal characters)
 * with 1 byte for each subcarrier. The value of each byte represents the subcarrier modulation for the
 * corresponding subcarrier.  See {{bibref|MoCAv2.0|section 14.3.6.3}} and {{bibref|MOCA20-MIB|Appendix
 * A}} for the encoding of this parameter.
 */
@JvmInline
@Generated
public value class MocaScMod(
    public val wrapped: ByteString,
) : DataType {
    public constructor(text: String) : this(text.decodeHex())

    override fun toString(): String = wrapped.hex()
}
