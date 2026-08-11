/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.util

/**
 * A thread safe sequence of Long values.
 */
expect class AtomicSequence(value: Long) {

    fun next(): Long
}