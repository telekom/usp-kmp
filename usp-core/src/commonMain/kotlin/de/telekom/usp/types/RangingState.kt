/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

/**
 * This data type defines the CM ranging state as reported by the CMTS. The enumerated values
 * associated with the RangingState are:  {{enum}}  See {{bibref|CM-SP-MULPIv3.0}}, Cable Modem - CMTS
 * Interaction.
 */
@Generated
public enum class RangingState(
    public val text: String,
    public val code: Int,
) : DataType {
    /**
     * indicates any state not described below
     */
    OTHER("Other", 1),
    /**
     * indicates that the CMTS has sent a ranging abort
     */
    ABORTED("Aborted", 2),
    /**
     * indicates that the CM ranging retry limit has exceeded
     */
    RETRIES_EXCEEDED("RetriesExceeded", 3),
    /**
     * indicates that the CMTS has sent a ranging success in the ranging response
     */
    SUCCESS("Success", 4),
    /**
     * indicates that the CMTS has sent a ranging continue in the ranging response
     */
    CONTINUE("Continue", 5),
    /**
     * indicates that the T4 timer expired on the CM
     */
    TIMEOUT_T_4("TimeoutT4", 6),
    ;

    public companion object {
        public fun from(text: String): RangingState? = entries.firstOrNull { it.text == text }

        public fun from(code: Int): RangingState? = entries.firstOrNull { it.code == code }
    }
}
