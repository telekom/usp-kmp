package de.telekom.usp.proto

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.proto.record.Record

class SessionContext(
    val from: EndpointIdentifier,
    val to: EndpointIdentifier,
    val sessionId: Long
) {

    var sequenceId = 0L

    fun isEndpointMatching(record: Record) = to.toShortString() == record.to_id
}