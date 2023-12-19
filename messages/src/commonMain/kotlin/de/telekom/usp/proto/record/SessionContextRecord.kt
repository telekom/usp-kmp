package de.telekom.usp.proto.record


val SessionContextRecord.hasPayload: Boolean
    get() = payload.isNotEmpty() && payload[0].size > 0