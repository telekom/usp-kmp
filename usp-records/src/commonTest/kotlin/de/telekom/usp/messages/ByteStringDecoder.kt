/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.messages

import de.telekom.usp.messages.proto.Msg
import de.telekom.usp.record.proto.Record
import kotlin.test.Test
import kotlin.test.assertNotNull

class ByteStringDecoder {

    @Test
    @OptIn(ExperimentalStdlibApi::class)
    fun `decode no session context`() {
        val bytes =
            "0a03312e30121f7573702f6167656e74732f70726f746f3a3a667269747a626f782d6b656d701a176f733a3a3030303430452d42304632303834304644324320003a4412420a0e0a0a32363538333938383731100312300a2e422c0a00100142260a063030303430451209465249545a21426f781a0c4230463230383430464432432203312e31"
        val rec = Record.ADAPTER.decode(bytes.hexToByteArray())
        println("Parsed USP record:          $rec")
        assertNotNull(rec.no_session_context)

        val msg = Msg.ADAPTER.decode(rec.no_session_context!!.payload)
        println("No session context message: $msg")
    }
}
