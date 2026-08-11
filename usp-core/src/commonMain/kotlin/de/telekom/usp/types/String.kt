/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

/**
 * Determines whether a USP parameter value represents a boolean value of `true`.
 */
public fun String?.isTrue(): Boolean = this != null && (this == "true" || this == "1")
