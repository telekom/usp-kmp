package de.telekom.usp.cli

import co.touchlab.kermit.Logger
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import de.telekom.usp.MessageExchange
import de.telekom.usp.isValidPath
import de.telekom.usp.messages.dsl.GetSupportedDm
import de.telekom.usp.messages.proto.GetSupportedDMResp
import de.telekom.usp.messages.proto.Msg

class GetSupportedDmCommand :
    AbstractCommand("get_supported_dm", "Send a message to get supported data model") {

    private val paths by option(
        "-p",
        "--path",
        help = "Path names to get supported data models for (might be repeated)"
    ).multiple(required = true).check("Invalid USP path specified") { paths: List<String> ->
        paths.all { isValidPath(it) }
    }

    private val isFirstLevelOnly by option(
        "-f",
        "--first-level-only",
        help = "Request first level only"
    ).flag(default = false)

    private val isSkipCommands by option(
        "--skip-commands",
        help = "Do not return commands"
    ).flag(default = false)

    private val isSkipEvents by option(
        "--skip-events",
        help = "Do not return events"
    ).flag(default = false)

    private val isSkipParams by option(
        "--skip-params",
        help = "Do not return params"
    ).flag(default = false)

    private fun createRequest(): Msg {
        return GetSupportedDm {
            addPath(*this@GetSupportedDmCommand.paths.toTypedArray())
            firstLevelOnly = isFirstLevelOnly
            returnCommands = !isSkipCommands
            returnEvents = !isSkipEvents
            returnParams = !isSkipParams
        }
    }

    private fun printResponse(response: GetSupportedDMResp) {
        Logger.i { "Result of \"$this\"" }
        response.req_obj_results.forEach { requestedObject ->
            Logger.i("Supported objects for:'${requestedObject.req_obj_path}'")
            requestedObject.supported_objs.forEach { supportedObject ->
                Logger.i {
                    "Supported object: ${supportedObject.supported_obj_path} " +
                            "(${supportedObject.access}, " +
                            "isMultiInstance=${supportedObject.is_multi_instance}, " +
                            "divergent paths: '${supportedObject.divergent_paths.joinToString()}')"
                }
                supportedObject.supported_commands.forEach { command ->
                    Logger.i { "${supportedObject.supported_obj_path}${command.command_name} (${command.command_type})" }
                }
                supportedObject.supported_events.forEach { event ->
                    Logger.i { "${supportedObject.supported_obj_path}${event.event_name}" }
                }
                supportedObject.supported_params.forEach { param ->
                    Logger.i { "${supportedObject.supported_obj_path}${param.param_name} (${param.access})" }
                }
                Logger.i("")
            }
        }
    }

    override suspend fun sendRequest(exchange: MessageExchange) {
        exchange.sendRequest(createRequest(), onError) { response: GetSupportedDMResp ->
            printResponse(response)
            onFinished()
        }
    }

    override fun toString(): String {
        return "get_supported_dm ${paths.joinToString(" ") { "-p $it" }}" +
                if (isFirstLevelOnly) " --first-level-only" else "" +
                        if (isSkipCommands) " --skip-commands" else "" +
                                if (isSkipEvents) " --skip-events" else "" +
                                        if (isSkipParams) " --skip-params" else ""
    }
}