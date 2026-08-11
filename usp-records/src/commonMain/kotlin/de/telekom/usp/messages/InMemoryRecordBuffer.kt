/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.messages

import de.telekom.usp.record.proto.SessionContextRecord
import de.telekom.usp.record.proto.isBegin
import okio.Buffer
import okio.BufferedSource

class InMemoryRecordBuffer : RecordBuffer {

    private val cache = mutableMapOf<Long, MutableMap<Long, SessionContextRecord>>()

    override fun put(record: SessionContextRecord) {
        val sessionCache = cache.getOrPut(record.session_id) { mutableMapOf() }
        sessionCache[record.sequence_id] = record
    }

    override fun fetch(sessionId: Long, sequenceId: Long): SessionContextRecord? {
        return cache[sessionId]?.get(sequenceId)
    }

    override fun payloadToBufferedSource(sessionId: Long, lastSequenceId: Long): BufferedSource {
        val records = buildList {
            for (sequenceId in lastSequenceId downTo 1) {
                val current = fetch(sessionId, sequenceId) ?: break
                add(current)
                if (current.isBegin) {
                    break
                }
            }
        }.reversed()

        return Buffer().apply {
            records.forEach { record ->
                record.payload.forEach { write(it) }
            }
        }
    }

    override fun clear(sessionId: Long, sequenceId: Long) {
        cache[sessionId]?.remove(sequenceId)
    }

    override fun clearSession(sessionId: Long) {
        cache.remove(sessionId)
    }

    override fun clearAll() {
        cache.clear()
    }

    override fun clearUpTo(sessionId: Long, sequenceIdLimit: Long) {
        cache[sessionId]?.let { records ->
            records.filterKeys { id -> id <= sequenceIdLimit }.forEach { entry ->
                records.remove(entry.key)
            }
        }
    }
}