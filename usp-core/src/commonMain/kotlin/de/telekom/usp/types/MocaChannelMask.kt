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
 * Represents one or more MoCA channel RF center frequencies using a hexadecimal encoded 64-bit
 * mask.  Bit 63 (the leftmost bit of the leftmost character) is the most significant bit (highest
 * frequency), and bit 0 (the rightmost bit of the rightmost character) is the least significant bit
 * (lowest frequency). Not all bits are valid MoCA channels.  Each bit represents 25 MHz of spectrum,
 * but the mapping from bits to frequencies varies with the MoCA version:  * MoCA 1.0 and MoCA 1.1:
 * bits 63 through 32 are not used, bit 31   represents 1575 MHz and bit 0 represents 800 MHz  * MoCA
 * 2.0 and MoCA 2.5: bit 63 represents 1975 MHz and bit 0 represents   400 MHz  For example, a MoCA 1.0
 * or MoCA 1.1 interface would use 0x000000001FFFC000 to represent 1150 MHz through 1500 MHz.  Note
 * that the MoCA version is indicated by the {{param|HighestVersion}} parameter.
 */
@JvmInline
@Generated
public value class MocaChannelMask(
    public val wrapped: ByteString,
) : DataType {
    public constructor(text: String) : this(text.decodeHex())

    override fun toString(): String = wrapped.hex()
}
