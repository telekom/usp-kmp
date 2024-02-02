package de.telekom.usp.messages

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.proto.record.Record
import kotlinx.datetime.Clock

class SessionContext(
    private val local: EndpointIdentifier,
    private val remote: EndpointIdentifier,
    val payloadSecurity: Record.PayloadSecurity,
    val sessionId: Long,
    clock: Clock = Clock.System
) {
    val creationTime = clock.now()

    // Our current sequence ID
    private var _sequenceId = 1L
    val sequenceId: Long
        get() = _sequenceId

    // The sequence ID we expect next from our remote
    private var _expectedId = 1L
    val expectedId: Long
        get() = _expectedId

    fun hasMatchingSession(sessionId: Long) = this.sessionId == sessionId

    fun incrementRemoteSequenceId(): Long {
        return _expectedId++
    }

    fun incrementLocalSequenceId(): Long {
        return _sequenceId++
    }

    fun isExpected(sequenceId: Long) = _expectedId == sequenceId

    fun isAhead(sequenceId: Long) = sequenceId > _expectedId

    override fun toString(): String {
        return "SessionContext(local=$local, remote=$remote, sessionId=$sessionId, localSequenceId=$_sequenceId, remoteSequenceId=$_expectedId, since=$creationTime)"
    }
}