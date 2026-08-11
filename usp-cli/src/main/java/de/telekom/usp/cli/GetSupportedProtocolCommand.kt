/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.cli

import co.touchlab.kermit.Logger
import com.github.ajalt.clikt.parameters.options.option
import de.telekom.usp.e2e.MessageExchange
import de.telekom.usp.messages.dsl.GetSupportedProtocol
import de.telekom.usp.messages.proto.GetSupportedProtocolResp
import de.telekom.usp.messages.proto.Msg

class GetSupportedProtocolCommand :
    AbstractCommand("get_supported_protocol", "Send a message to retrieve the supported protocol") {

    private val versions by option(
        "-v",
        "--versions",
        help = "The versions supported by this controller (comma separated list)"
    )

    private fun createRequest(): Msg {
        return GetSupportedProtocol(versions ?: "")
    }

    override suspend fun sendRequest(exchange: MessageExchange) {
        exchange.sendRequest(createRequest(), onError) { response: GetSupportedProtocolResp ->
            Logger.i { "Result of \"this\"" }
            Logger.i { "Agent supported protocol versions: '${response.agent_supported_protocol_versions}'" }
            onFinished()
        }
    }

    override fun toString(): String {
        return "get_supported_protocol" + if (versions != null) " -v $versions" else ""
    }
}