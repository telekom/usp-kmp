/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import okio.ByteString
import okio.ByteString.Companion.decodeHex
import kotlin.jvm.JvmInline

/**
 * This data type represents a single TLV encoding. This first octet represents the Type of the TLV.
 * The second octet represents an unsigned 8-bit Length of the subsequent Value part of the TLV. The
 * remaining octets represent the value. The Value could be an atomic value or a sequence of one or
 * more sub-TLVs.  See {{bibref|CM-SP-MULPIv3.0}}, Common Radio Frequency Interface Encodings Annex.
 */
@JvmInline
@Generated
public value class TLV8(
    public val wrapped: ByteString,
) : DataType {
    public constructor(text: String) : this(text.decodeHex())

    override fun toString(): String = wrapped.hex()
}
