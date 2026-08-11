/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.messages

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random


object SessionIdFactory {

    private val consumed = mutableSetOf<Long>()

    private val mutex = Mutex()

    suspend fun next(): Long {
        return mutex.withLock {
            var candidate = nextSessionId()
            while (consumed.contains(candidate)) {
                candidate = nextSessionId()
            }

            consumed.add(candidate)
            candidate
        }
    }

    suspend fun consume(sessionId: Long) {
        mutex.withLock {
            consumed.add(sessionId)
        }
    }

    // R-E2E.3: session IDs must be greater than 1 and scoped to the remote USP Endpoint
    private fun nextSessionId() = Random.nextLong(2, Long.MAX_VALUE)
}