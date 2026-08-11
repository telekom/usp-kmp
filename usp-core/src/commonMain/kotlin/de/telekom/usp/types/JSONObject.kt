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
 * A JSON Object as defined in {{bibref|RFC7159|Section 4}}.
 */
@JvmInline
@Generated
public value class JSONObject(
    public val wrapped: String,
) : DataType {
    override fun toString(): String = wrapped
}
