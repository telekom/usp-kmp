package de.telekom.usp.messages

import de.telekom.usp.EndpointIdentifier
import kotlinx.datetime.Clock

class SessionContext(
    val from: EndpointIdentifier,
    val sessionId: Long,
    clock: Clock = Clock.System
) {
    val creationTime = clock.now()

    var sequenceId = 1L

    fun hasMatchingSession(sessionId: Long) = this.sessionId == sessionId

    fun restartWith(sessionId: Long): SessionContext {
        // [R-E2E.6] when restarting session, the sequence ID must be reset to 1
        return SessionContext(from, sessionId)
    }

    override fun toString(): String {
        return "SessionContext(from=$from, sessionId=$sessionId, sequenceId=$sequenceId)"
    }
}