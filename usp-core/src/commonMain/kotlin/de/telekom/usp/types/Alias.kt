/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * A non-volatile unique key used to reference this instance. Alias provides a mechanism for a
 * Controller to label this instance for future reference.  The following mandatory constraints MUST be
 * enforced:  * The value MUST NOT be empty.  * The value MUST start with a letter.  * If the value is
 * not assigned by the Controller at creation time, the   Agent MUST assign a value with an "cpe-"
 * prefix.
 */
@JvmInline
@Generated
public value class Alias(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped
}
