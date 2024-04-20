//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

/**
 * Represents the MoCA Bands and sub-bands the device is configured to operate in or that the device
 * supports. {{enum}}  See {{bibref|MoCAv2.0|section 15}}.
 */
@Generated
public enum class MocaBand(
    public val text: String,
    public val code: Int,
) : DataType {
    RESERVED("reserved", 0),
    BAND_FCBL("bandFCBL", 1),
    BAND_FSAT("bandFSAT", 2),
    BAND_E("bandE", 3),
    BAND_EX_D("bandExD", 4),
    BAND_DH("bandDH", 5),
    BAND_DL("bandDL", 6),
    NO_BAND("noBand", 7),
    ;

    public companion object {
        public fun from(text: String): MocaBand? = entries.firstOrNull { it.text == text }

        public fun from(code: Int): MocaBand? = entries.firstOrNull { it.code == code }
    }
}
