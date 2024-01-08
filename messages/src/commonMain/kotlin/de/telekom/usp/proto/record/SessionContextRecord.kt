package de.telekom.usp.proto.record


val SessionContextRecord.containsRetransmitRequest: Boolean
    get() = retransmit_id > 0

val SessionContextRecord.hasPayload: Boolean
    get() = payload.isNotEmpty() && payload[0].size > 0