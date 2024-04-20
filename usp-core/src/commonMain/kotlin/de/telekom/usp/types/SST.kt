//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

/**
 * Service Slice Type (SST). {{enum}}  See {{bibref|3GPP-TS.23.501|Clause 5.15.2.2}}.
 */
@Generated
public enum class SST(
    public val text: String,
) : DataType {
    /**
     * 5G Enhanced Mobile Broadband
     */
    E_MBB("eMBB"),

    /**
     * Ultra-Reliable Low Latency Communications
     */
    URLLC("URLLC"),

    /**
     * Massive IoT
     */
    MIO_T("MIoT"),

    /**
     * Vehicle to Everything
     */
    V_2_X("V2X"),
    ;

    public companion object {
        public fun from(text: String): SST? = entries.firstOrNull { it.text == text }
    }
}
