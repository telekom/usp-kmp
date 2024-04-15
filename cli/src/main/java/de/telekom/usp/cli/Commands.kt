package de.telekom.usp.cli

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
import de.telekom.usp.isValidPath
import de.telekom.usp.messages.dsl.Add
import de.telekom.usp.messages.dsl.Get
import de.telekom.usp.messages.dsl.GetInstances
import de.telekom.usp.messages.dsl.GetSupportedDm
import de.telekom.usp.messages.dsl.Set
import de.telekom.usp.messages.dsl.required
import de.telekom.usp.messages.proto.Msg

val commands = listOf(
    GetCommand(), GetSupportedDmCommand(), GetInstancesCommand(), SetCommand(), AddCommand()
)

class GetCommand : RequestCommand("get", "Send a get message") {
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

    override fun createMsg(): Msg {
        return Get {
            addPath(*this@GetCommand.paths.toTypedArray())
            maxDepth = depth
        }
    }
}

class GetSupportedDmCommand :
    RequestCommand("get_supported_dm", "Send a get supported data model message") {

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

    override fun createMsg(): Msg {
        return GetSupportedDm {
            addPath(*this@GetSupportedDmCommand.paths.toTypedArray())
            firstLevelOnly = isFirstLevelOnly
            returnCommands = !isSkipCommands
            returnEvents = !isSkipEvents
            returnParams = !isSkipParams
        }
    }
}

class GetInstancesCommand : RequestCommand("get_instances", "Send a get instances message") {

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

    override fun createMsg(): Msg {
        return GetInstances {
            addPath(*this@GetInstancesCommand.paths.toTypedArray())
            this.firstLevelOnly = isFirstLevelOnly
        }
    }
}

class SetCommand : RequestCommand("set", "Send a set message") {
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

    override fun createMsg(): Msg {
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
}

class AddCommand : RequestCommand("add", "Send an add message") {
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

    override fun createMsg(): Msg {
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
}

abstract class RequestCommand(name: String, help: String) : CliktCommand(name = name, help = help) {

    var requestExecutor: ((Msg) -> Unit)? = null

    override fun run() {
        val request = createMsg()
        requestExecutor?.let { it(request) }
    }

    abstract fun createMsg(): Msg
}
