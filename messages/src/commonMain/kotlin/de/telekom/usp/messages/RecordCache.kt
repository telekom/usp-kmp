package de.telekom.usp.messages

import de.telekom.usp.proto.record.SessionContextRecord
import de.telekom.usp.proto.record.isComplete
import okio.BufferedSource


interface RecordCache {

    fun put(record: SessionContextRecord)

    fun fetch(sessionId: Long, sequenceId: Long): SessionContextRecord?

    fun payloadToBufferedSource(sessionId: Long, sequenceIds: LongRange): BufferedSource

    fun clear(sessionId: Long, sequenceId: Long)

    fun clearSession(sessionId: Long)

    fun clearAll()

    fun clearUpTo(sessionId: Long, sequenceIdLimit: Long)
}

fun RecordCache.missingSequenceIds(sessionId: Long, sequenceIds: LongRange): List<Long> {
    return sequenceIds.filter { fetch(sessionId, it) == null }
}

fun RecordCache.hasAllSegmentedRecords(sessionId: Long, segmentationBeginId: Long): Boolean {
    if (segmentationBeginId < 0) {
        return false
    }

    for (sequenceId in segmentationBeginId..Long.MAX_VALUE) {
        val record = fetch(sessionId, sequenceId)
        if (record == null) {
            break
        } else if (record.isComplete) {
            return true
        }
    }
    return false
}

