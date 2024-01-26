package de.telekom.usp.messages

import de.telekom.usp.proto.record.SessionContextRecord
import okio.Buffer
import okio.BufferedSource

class InMemoryRecordCache : RecordCache {

    private val cache = mutableMapOf<Long, MutableMap<Long, SessionContextRecord>>()

    override fun put(record: SessionContextRecord) {
        val sessionCache = cache.getOrPut(record.session_id) { mutableMapOf() }
        sessionCache[record.sequence_id] = record
    }

    override fun fetch(sessionId: Long, sequenceId: Long): SessionContextRecord? {
        return cache[sessionId]?.get(sequenceId)
    }

    override fun payloadToBufferedSource(sessionId: Long, sequenceIds: LongRange): BufferedSource {
        return Buffer().apply {
            sequenceIds.forEach { sequenceId ->
                fetch(sessionId, sequenceId)?.let { record ->
                    record.payload.forEach { write(it) }
                }
            }
        }
    }

    override fun clear(sessionId: Long, sequenceId: Long) {
        cache[sessionId]?.remove(sequenceId)
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