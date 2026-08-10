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
 * Indicates the DOCSIS Upstream Channel Type.
 */
@Generated
public enum class DocsisUpstreamType(
    public val text: String,
    public val code: Int,
) : DataType {
    /**
     * Information not available
     */
    UNKNOWN("Unknown", 0),
    /**
     * Time Division Multiple Access
     */
    TDMA("TDMA", 1),
    /**
     * Advanced Time Division Multiple Access
     */
    ATDMA("ATDMA", 2),
    /**
     * Synchronous Code Division Multiple Access
     */
    SCDMA("SCDMA", 3),
    /**
     * Simultaneous support of TDMA and A-TDMA modes
     */
    TDMAAND_ATDMA("TDMAAndATDMA", 4),
    ;

    public companion object {
        public fun from(text: String): DocsisUpstreamType? = entries.firstOrNull { it.text == text }

        public fun from(code: Int): DocsisUpstreamType? = entries.firstOrNull { it.code == code }
    }
}
