package de.telekom.usp.messages

import de.telekom.usp.EndpointIdentifier
import kotlinx.datetime.Clock

class SessionContext(
    val from: EndpointIdentifier,
    val sessionId: Long,
    private val clock: Clock = Clock.System
) {
    val creationTime = clock.now()

    var sequenceId = 1L

    fun isSessionMatching(recordSessionId: Long) = sessionId == recordSessionId

    fun restartWith(sessionId: Long) : SessionContext = SessionContext(from, sessionId)

    override fun toString(): String {
        return "SessionContext(from=$from, sessionId=$sessionId, sequenceId=$sequenceId)"
    }
}