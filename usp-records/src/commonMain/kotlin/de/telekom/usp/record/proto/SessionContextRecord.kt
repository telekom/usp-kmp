/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.record.proto

import de.telekom.usp.record.proto.SessionContextRecord.PayloadSARState
import okio.Buffer
import okio.BufferedSource


val SessionContextRecord.containsRetransmitRequest: Boolean
    get() = retransmit_id > 0

val SessionContextRecord.hasPayload: Boolean
    get() = payload.isNotEmpty() && payload[0].size > 0

val SessionContextRecord.isSingleRecord: Boolean
    get() = payload_sar_state == PayloadSARState.NONE && payloadrec_sar_state == PayloadSARState.NONE

val SessionContextRecord.isComplete: Boolean
    get() = payload_sar_state == PayloadSARState.COMPLETE

val SessionContextRecord.isBegin: Boolean
    get() = payload_sar_state == PayloadSARState.BEGIN

fun SessionContextRecord.payloadToBufferedSource(): BufferedSource {
    require(payload.isNotEmpty()) { "Trying to read bytes from empty payload" }

    return Buffer().apply {
        payload.forEach { write(it) }
    }
}