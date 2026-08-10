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
 * Cut-down version of SNMP RowStatus that supports only its "status" values, not its "control"
 * values.
 */
@Generated
public enum class RowStatus(
    public val text: String,
    public val code: Int,
) : DataType {
    /**
     * The row is available for use by the managed device.
     */
    ACTIVE("active", 1),
    /**
     * The row exists in the Agent, but is unavailable for use by the managed device (see NOTE
     * below); "notInService" has no implication regarding the internal consistency of the row,
     * availability of resources, or consistency with the current state of the managed device.
     */
    NOT_IN_SERVICE("notInService", 2),
    /**
     * The row exists in the Agent, but is missing information necessary in order to be available
     * for use by the managed device, i.e., one or more required parameter in the row have not been
     * populated.
     */
    NOT_READY("notReady", 3),
    /**
     * Not used.
     */
    CREATE_AND_GO("createAndGo", 4),
    /**
     * Not used.
     */
    CREATE_AND_WAIT("createAndWait", 5),
    /**
     * Not used.
     */
    DESTROY("destroy", 6),
    ;

    public companion object {
        public fun from(text: String): RowStatus? = entries.firstOrNull { it.text == text }

        public fun from(code: Int): RowStatus? = entries.firstOrNull { it.code == code }
    }
}
