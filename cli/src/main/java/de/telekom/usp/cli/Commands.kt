package de.telekom.usp.cli

import co.touchlab.kermit.Logger
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import de.telekom.usp.MessageExchange
import de.telekom.usp.MessageExchangeFailure
import de.telekom.usp.isValidPath
import de.telekom.usp.messages.dsl.Add
import de.telekom.usp.messages.dsl.Get
import de.telekom.usp.messages.dsl.GetInstances
import de.telekom.usp.messages.dsl.GetSupportedDm
import de.telekom.usp.messages.dsl.Set
import de.telekom.usp.messages.dsl.required
import de.telekom.usp.messages.proto.AddResp
import de.telekom.usp.messages.proto.GetInstancesResp
import de.telekom.usp.messages.proto.GetResp
import de.telekom.usp.messages.proto.GetSupportedDMResp
import de.telekom.usp.messages.proto.Msg
import de.telekom.usp.messages.proto.SetResp
import de.telekom.usp.messages.proto.debugMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicLong

val commands = listOf(
    GetCommand(), GetSupportedDmCommand(), GetInstancesCommand(), SetCommand(), AddCommand()
)

class GetCommand : UspCommand("get", "Send a get message") {
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
            addPath(*this@GetCommand.paths.toTypedArray())
            maxDepth = depth
        }
    }

    override suspend fun MessageExchange.sendRequest() {
        sendRequest(createRequest(), onError) { response: GetResp ->
            Logger.i { response.debugMessage() }
            onFinished()
        }
    }
}

class GetSupportedDmCommand :
    UspCommand("get_supported_dm", "Send a get supported data model message") {

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

    override suspend fun MessageExchange.sendRequest() {
        sendRequest(createRequest(), onError) { response: GetSupportedDMResp ->
            Logger.i { response.toString() }
            onFinished()
        }
    }
}

class GetInstancesCommand : UspCommand("get_instances", "Send a get instances message") {

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

    override suspend fun MessageExchange.sendRequest() {
        sendRequest(createRequest(), onError) { response: GetInstancesResp ->
            Logger.i { response.toString() }
            onFinished()
        }
    }
}

class SetCommand : UspCommand("set", "Send a set message") {
    private val path by option("-p", "--path", help = "Path to SET values for").required()

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
        "-a",
        "--allow-partial",
        help = "Allow partial updates"
    ).flag(default = false)

    private fun createRequest(): Msg {
        if (values.isEmpty() && required.isEmpty()) {
            throw PrintMessage("At least one parameter must be specified (using -p or -r)")
        }

        return Set {
            allowPartial = isAllowPartial
            addPath(path) {
                required.forEach { param ->
                    params[param.key] = param.value required true
                }
                values.forEach { param ->
                    params[param.key] = param.value required false
                }
            }
        }
    }

    override suspend fun MessageExchange.sendRequest() {
        sendRequest(createRequest(), onError) { response: SetResp ->
            Logger.i { response.toString() }
            onFinished()
        }
    }
}

class AddCommand : UspCommand("add", "Send an add message") {
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
        "-a",
        "--allow-partial",
        help = "Allow partial adding"
    ).flag(default = false)

    private fun createRequest(): Msg {
        if (values.isEmpty() && required.isEmpty()) {
            throw PrintMessage("At least one parameter must be specified (using -p or -r)")
        }

        return Add {
            allowPartial = isAllowPartial
            addPath(path) {
                required.forEach { param ->
                    params[param.key] = param.value required true
                }
                values.forEach { param ->
                    params[param.key] = param.value required false
                }
            }
        }
    }

    override suspend fun MessageExchange.sendRequest() {
        sendRequest(createRequest(), onError) { response: AddResp ->
            Logger.i { response.toString() }
            onFinished()
        }
    }
}

abstract class UspCommand(name: String, help: String) : CliktCommand(name = name, help = help) {

    // Will get initialized by the Main command!
    lateinit var exchange: MessageExchange

    // Will get initialized by the Main command!
    lateinit var timeout: AtomicLong

    private val waitUntil = MutableSharedFlow<Unit>()

    open val onError: (MessageExchangeFailure) -> Unit = { failure ->
        when (failure) {
            is MessageExchangeFailure.ResponseError -> {
                val err = failure.error
                Logger.e { "Received a USP error message: ${err.err_msg} (${err.err_code})" }
            }

            is MessageExchangeFailure.TimeoutOccurred -> {
                Logger.e("Timeout occurred while waiting for a response")
            }
        }

        //onFinished()
    }

    override fun run() {
        runBlocking {
            exchange.start()
            exchange.sendRequest()
            println(timeout.get())
            val success = withTimeoutOrNull(timeout.get()) {
                waitUntil.first() // Waits for onFinished to be called
                true
            }

            exchange.stop()
            if (success != true) {
                Logger.w { "Timeout occurred while waiting for response" }
            }
        }
    }

    protected fun onFinished() {
        runBlocking {
            waitUntil.emit(Unit)
        }
    }

    /**
     * Sends the actual request message via one of the `sendRequest` methods of this and prints the
     * results. Must call [onFinished] when a result has been received
     */
    abstract suspend fun MessageExchange.sendRequest()
}
