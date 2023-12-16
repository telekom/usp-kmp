package de.telekom.usp.proto

import de.telekom.usp.EndpointIdentifier

class SessionContext(
    val from: EndpointIdentifier,
    val to: EndpointIdentifier,
    val sessionId: Long) {

    var sequenceId = 0L
}