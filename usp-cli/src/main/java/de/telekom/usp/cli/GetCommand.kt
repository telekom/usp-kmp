/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.cli

import co.touchlab.kermit.Logger
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import de.telekom.usp.e2e.MessageExchange
import de.telekom.usp.isValidPath
import de.telekom.usp.messages.dsl.Get
import de.telekom.usp.messages.proto.GetResp
import de.telekom.usp.messages.proto.Msg

class GetCommand : AbstractCommand("get", "Send a get message") {

    private val paths by option(
        "-p",
        "--path",
        help = "Path names to GET data from (might be repeated)"
    ).multiple(required = true).check("Invalid USP path specified") { paths: List<String> ->
        paths.all { isValidPath(it) }
    }

    private val depth by option(
        "-m",
        "--max",
        help = "Max depth for GET message (default is 1)"
    ).int().default(1)

    private fun createRequest(): Msg {
        return Get {
            maxDepth = depth
            paths(*this@GetCommand.paths.toTypedArray())
        }
    }

    private fun printResponse(response: GetResp) {
        Logger.i { "Result of \"$this\"" }
        response.req_path_results.toGetResult().forEach {
            Logger.i(it.toString())
        }
    }

    override suspend fun sendRequest(exchange: MessageExchange) {
        exchange.sendRequest(createRequest(), onError) { response: GetResp ->
            printResponse(response)
            onFinished()
        }
    }

    override fun toString(): String {
        return "get ${paths.joinToString(" ") { "-p $it" }}" +
                " --max $depth"
    }
}