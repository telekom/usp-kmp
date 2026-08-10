/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.messages

import de.telekom.usp.messages.MessageIdFactory.next
import de.telekom.usp.util.AtomicSequence

/**
 * Creates unique message IDs for every call of [next]. This object is thread safe.
 */
object MessageIdFactory {

    private val sequence = AtomicSequence(0)

    fun next(prefix: String = "") = prefix + sequence.next()
}