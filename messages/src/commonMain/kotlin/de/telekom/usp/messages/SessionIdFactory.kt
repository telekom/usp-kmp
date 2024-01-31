package de.telekom.usp.messages

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


object SessionIdFactory {

    private val mutex = Mutex()

    // R-E2E.3: session IDs must be greater than 1 and scoped to the remote USP Endpoint
    private var sessionId: Long = 2L

    suspend fun next(): Long {
        mutex.withLock {
            return sessionId++
        }
    }
}