package de.telekom.usp.messages

import okio.ByteString

class InMemoryRecordCache : RecordCache {

    private val cache = mutableMapOf<Long, MutableMap<Long, ByteString>>()

    override fun set(sessionId: Long, sequenceId: Long, record: ByteString) {
        val sessionCache = cache.getOrPut(sessionId) { mutableMapOf() }
        sessionCache[sequenceId] = record
    }

    override fun get(sessionId: Long, sequenceId: Long): ByteString? {
        return cache[sessionId]?.get(sequenceId)
    }

    override fun clearAll(sessionId: Long) {
        if (sessionId == -1L) {
            cache.clear()
        } else {
            cache.remove(sessionId)
        }
    }

    override fun clearUpTo(sessionId: Long, sequenceIdLimit: Long) {
        cache[sessionId]?.let { records ->
            records.filterKeys { id -> id <= sequenceIdLimit }.forEach { entry ->
                records.remove(entry.key)
            }
        }
    }
}