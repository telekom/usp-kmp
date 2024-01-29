package de.telekom.usp.messages

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.proto.record.Record
import kotlinx.datetime.Clock

class SessionContext(
    val from: EndpointIdentifier,
    val payloadSecurity: Record.PayloadSecurity,
    val sessionId: Long,
    clock: Clock = Clock.System
) {
    val creationTime = clock.now()

    private var _sequenceId = 1L
    val sequenceId: Long
        get() = _sequenceId

    fun hasMatchingSession(sessionId: Long) = this.sessionId == sessionId

    fun restartWith(sessionId: Long): SessionContext {
        // [R-E2E.6] when restarting session, the sequence ID must be reset to 1
        return SessionContext(from, payloadSecurity, sessionId)
    }

    fun incrementSequenceId() {
        _sequenceId++
    }

    fun isExpected(sequenceId: Long) = _sequenceId == sequenceId

    fun isAhead(sequenceId: Long) = sequenceId > _sequenceId

    override fun toString(): String {
        return "SessionContext(from=$from, sessionId=$sessionId, sequenceId=$_sequenceId)"
    }
}