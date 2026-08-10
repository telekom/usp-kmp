/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import kotlin.jvm.JvmInline

/**
 * Represents a measure of power in mW expressed in decibels, and calculated as follows:   power =
 * 10*log10( Vrms^2 / R * 1000 )   where Vrms is the root-mean-square Voltage of the received waveform
 * and  R is 75 ohms.
 */
@JvmInline
@Generated
public value class MocaDBm(
    public val wrapped: Int,
) : DataType {
    public constructor(text: String) : this(text.toInt())

    override fun toString(): String = wrapped.toString()
}
