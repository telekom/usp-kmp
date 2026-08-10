/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.cli

import co.touchlab.kermit.Logger
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import de.telekom.usp.e2e.MessageExchange
import de.telekom.usp.isValidPath
import de.telekom.usp.messages.dsl.Delete
import de.telekom.usp.messages.proto.DeleteResp
import de.telekom.usp.messages.proto.Msg

class DeleteCommand : AbstractCommand("delete", "Send a delete message") {

    private val paths by option(
        "-p",
        "--path",
        help = "Multi instance object path names to delete (might be repeated)"
    ).multiple(required = true).check("Invalid USP path specified") { paths: List<String> ->
        paths.all { isValidPath(it) }
    }

    private val isAllowPartial by option(
        "--allow-partial",
        help = "Delete all valid object, regardless of the inability to delete one or more objects"
    ).flag()

    private fun createRequest(): Msg {
        return Delete {
            allowPartial = isAllowPartial
            paths(*this@DeleteCommand.paths.toTypedArray())
        }
    }

    private fun printResponse(response: DeleteResp) {
        Logger.i { "Result of \"$this\"" }
        response.deleted_obj_results.forEach { result ->
            if (result.oper_status?.oper_failure != null) {
                val failure = result.oper_status?.oper_failure!!
                Logger.i { "Failed to delete '${result.requested_path}': ${failure.err_msg} (${failure.err_code})" }

            } else if (result.oper_status?.oper_success != null) {
                val success = result.oper_status?.oper_success!!
                Logger.i { "For '${result.requested_path}' successfully deleted '${success.affected_paths}'" }

                success.unaffected_path_errs.forEach { unaffected ->
                    Logger.i {
                        "For '${result.requested_path}' did not delete '${unaffected.unaffected_path}' " +
                                "${unaffected.err_msg} (${unaffected.err_code})"
                    }
                }
            }
        }
    }

    override suspend fun sendRequest(exchange: MessageExchange) {
        exchange.sendRequest(createRequest(), onError) { response: DeleteResp ->
            printResponse(response)
            onFinished()
        }
    }

    override fun toString(): String {
        return "delete ${paths.joinToString(" ") { "-p $it" }}" +
                if (isAllowPartial) " --allow-partial" else ""
    }
}