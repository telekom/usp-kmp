package de.telekom.usp.proto.record

import de.telekom.usp.proto.record.SessionContextRecord.PayloadSARState
import okio.Buffer


val SessionContextRecord.containsRetransmitRequest: Boolean
    get() = retransmit_id > 0

val SessionContextRecord.hasPayload: Boolean
    get() = payload.isNotEmpty() && payload[0].size > 0

val SessionContextRecord.isSingleRecord: Boolean
    get() = payload_sar_state == PayloadSARState.NONE && payloadrec_sar_state == PayloadSARState.NONE

fun SessionContextRecord.collectPayload(buffer: Buffer): Buffer {
    require(payload.isNotEmpty()) { "Trying to read bytes from empty payload" }

    return buffer.apply {
        payload.forEach { write(it) }
    }
}