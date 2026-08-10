/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.messages

import de.telekom.usp.record.proto.SessionContextRecord
import de.telekom.usp.record.proto.isComplete
import okio.BufferedSource


interface RecordBuffer {

    fun put(record: SessionContextRecord)

    fun fetch(sessionId: Long, sequenceId: Long): SessionContextRecord?

    fun payloadToBufferedSource(sessionId: Long, lastSequenceId: Long): BufferedSource

    fun clear(sessionId: Long, sequenceId: Long)

    fun clearSession(sessionId: Long)

    fun clearAll()

    fun clearUpTo(sessionId: Long, sequenceIdLimit: Long)
}

fun RecordBuffer.missingSequenceIds(sessionId: Long, sequenceIds: LongRange): List<Long> {
    return sequenceIds.filter { fetch(sessionId, it) == null }
}

fun RecordBuffer.hasAllSegmentedRecords(sessionId: Long, segmentationBeginId: Long): Boolean {
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

