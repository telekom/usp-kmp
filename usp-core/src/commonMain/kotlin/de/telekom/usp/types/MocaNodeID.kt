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
 * Represents a MoCA Node ID.  * MoCA 1.0 network can have a maximum of 8 MoCA Nodes, so Node ID is
 * 0 to   7.  * MoCA 1.1, MoCA 2.0, or MoCA 2.5 network can have a maximum of 16 MoCA   Nodes, so Node
 * ID is 0 to 15.
 */
@JvmInline
@Generated
public value class MocaNodeID(
    public val wrapped: UInt,
) : DataType {
    public constructor(text: String) : this(text.toUInt())

    override fun toString(): String = wrapped.toString()
}
