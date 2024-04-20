//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import okio.ByteString
import okio.ByteString.Companion.decodeHex
import kotlin.jvm.JvmInline

/**
 * This data type represents the equalizer data as measured at the receiver interface. The format of
 * the equalizer follows the structure of the Transmit Equalization Adjust RNG-RSP TLV of DOCSIS RFI
 * v2.0.  The equalizer coefficients are considered signed 16-bit integers in the range from -32768
 * (0x8000) to 32767 (0x7FFF).  DOCSIS specifications require up to a maximum of 64 equalizer taps (n +
 * m); therefore, this object size can be up to 260 bytes (4 + 4x64). The minimum object size (other
 * than zero) for a t-spaced tap with a minimum of 8 symbols will be 36 (4 + 4x8).  See
 * {{bibref|CM-SP-RFIv2.0|Figure 8-23}}.
 */
@JvmInline
@Generated
public value class DocsEqualizerData(
    public val wrapped: ByteString,
) : DataType {
    public constructor(text: String) : this(text.decodeHex())

    override fun toString(): String = wrapped.hex()
}
