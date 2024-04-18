package de.telekom.usp.cli

import co.touchlab.kermit.Logger
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import de.telekom.usp.MessageExchange
import de.telekom.usp.isValidPath
import de.telekom.usp.messages.dsl.GetInstances
import de.telekom.usp.messages.proto.GetInstancesResp
import de.telekom.usp.messages.proto.Msg

class GetInstancesCommand : AbstractCommand("get_instances", "Send a get instances message") {

    private val paths by option(
        "-p",
        "--path",
        help = "Path names to get instances for (might be repeated)"
    ).multiple(required = true).check("Invalid USP path specified") { paths: List<String> ->
        paths.all { isValidPath(it) }
    }

    private val isFirstLevelOnly by option(
        "-f",
        "--first-level-only",
        help = "Request first level only"
    ).flag(default = false)

    private fun createRequest(): Msg {
        return GetInstances {
            addPath(*this@GetInstancesCommand.paths.toTypedArray())
            this.firstLevelOnly = isFirstLevelOnly
        }
    }

    private fun printResponse(response: GetInstancesResp) {
        Logger.i { "Result of get instances message for: ${paths.joinToString()} (firstLevelOnly=$isFirstLevelOnly)" }
        response.req_path_results.toGetInstancesResult().forEach {
            Logger.i(it.toString())
        }
    }

    override suspend fun sendRequest(exchange: MessageExchange) {
        exchange.sendRequest(createRequest(), onError) { response: GetInstancesResp ->
            printResponse(response)
            onFinished()
        }
    }
}