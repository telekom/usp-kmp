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
 * The value is measured in ''dBm/1000'', i.e. the value divided by 1000 is dB relative to 1 mW. For
 * example, -12345 means -12.345 dBm, 0 means 0 dBm (1 mW) and 12345 means 12.345 dBm.
 */
@JvmInline
@Generated
public value class Dbm1000(
    public val wrapped: Int,
) : DataType {
    public constructor(text: String) : this(text.toInt())

    override fun toString(): String = wrapped.toString()
}
