/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.cli

import co.touchlab.kermit.Logger
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import de.telekom.usp.e2e.MessageExchange
import de.telekom.usp.messages.dsl.Add
import de.telekom.usp.messages.dsl.required
import de.telekom.usp.messages.proto.AddResp
import de.telekom.usp.messages.proto.Msg

class AddCommand : AbstractCommand("add", "Send an add message") {
    private val path by option("-p", "--path", help = "Path to ADD values for").required()

    private val values by option(
        "-v",
        "--value",
        help = "Specify non required parameter as key=value"
    ).associate()

    private val required by option(
        "-r",
        "--required-value",
        help = "Specify required parameter as key=value"
    ).associate()

    private val isAllowPartial by option(
        "--allow-partial",
        help = "Allow partial adding"
    ).flag(default = false)

    private fun createRequest(): Msg {
        if (values.isEmpty() && required.isEmpty()) {
            throw PrintMessage("At least one parameter must be specified (using -p or -r)")
        }

        return Add {
            allowPartial = isAllowPartial
            path(path) {
                params.putAll(required.map { it.key to (it.value required true) })
                params.putAll(values.map { it.key to (it.value required false) })
            }
        }
    }

    private fun printResponse(response: AddResp) {
        Logger.i { "Result of \"$this\"" }
        response.created_obj_results.toAddResult().forEach {
            Logger.i(it.toString())
        }
    }

    override suspend fun sendRequest(exchange: MessageExchange) {
        exchange.sendRequest(createRequest(), onError) { response: AddResp ->
            printResponse(response)
            onFinished()
        }
    }

    override fun toString(): String {
        return "add -p $path" +
                values.entries.joinToString(" ", " ") { "-v ${it.key}=${it.value}" } +
                required.entries.joinToString(" ", " ") { "-r ${it.key}=${it.value}" } +
                if (isAllowPartial) " --allow-partial" else ""
    }
}