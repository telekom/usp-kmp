package de.telekom.usp.messages

import okio.ByteString


interface ByteCache {

    operator fun set(sessionId: Long, sequenceId: Long, record: ByteString)

    operator fun get(sessionId: Long, sequenceId: Long): ByteString?

    fun clearAll(sessionId: Long = -1L)

    fun clearUpTo(sessionId: Long, sequenceIdLimit: Long)
}