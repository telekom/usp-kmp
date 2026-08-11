/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.telekom.usp.util

import java.util.concurrent.atomic.AtomicLong

actual class AtomicSequence actual constructor(value: Long) {

    private val value = AtomicLong(value)

    actual fun next() = value.incrementAndGet()
}