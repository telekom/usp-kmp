/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.cli

import co.touchlab.kermit.Logger
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import de.telekom.usp.e2e.MessageExchange
import de.telekom.usp.messages.dsl.Operate
import de.telekom.usp.messages.proto.Msg
import de.telekom.usp.messages.proto.OperateResp

class OperateCommand : AbstractCommand("operate", "Send an operate message") {

    private val command by option("-c", "--command", help = "Command to execute").required()

    private val commandKey by option(
        "-k",
        "--key",
        help = "Command key to use, default is empty string"
    )

    private val params by option(
        "-p",
        "--param",
        help = "Specify operate parameter as key=value"
    ).associate()

    private fun createRequest(): Msg {
        return Operate(command, commandKey ?: "", true) {
            args.putAll(params)
        }
    }

    private fun printResponse(resp: OperateResp) {
        Logger.i { "Result of \"$this\"" }
        resp.operation_results.forEach { result ->
            Logger.i { "Command executed: ${result.executed_command} for path: '${result.req_obj_path ?: ""}'" }
            if ((result.req_output_args?.output_args?.size ?: 0) > 0) {
                Logger.i { "Output args: ${result.req_output_args?.output_args}" }
            }
            if (result.cmd_failure?.err_code != 0) {
                Logger.i { "Error occurred: ${result.cmd_failure?.err_msg} (${result.cmd_failure?.err_code})" }
            }
            Logger.i("")
        }
    }

    override suspend fun sendRequest(exchange: MessageExchange) {
        exchange.sendRequest(createRequest(), onError) { response: OperateResp ->
            printResponse(response)
            onFinished()
        }
    }

    override fun toString(): String {
        return "operate -c $command" + if (commandKey != null) " -k $commandKey" else "" +
                params.entries.joinToString(" ", " ") { "-p ${it.key}=${it.value}" }
    }
}