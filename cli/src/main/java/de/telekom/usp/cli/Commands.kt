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
import de.telekom.usp.messages.proto.Add
import de.telekom.usp.messages.proto.Get
import de.telekom.usp.messages.proto.GetInstances
import de.telekom.usp.messages.proto.GetSupportedDM
import de.telekom.usp.messages.proto.Request
import de.telekom.usp.messages.proto.Set

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

    private val maxDepth by option(
        "-m",
        "--max",
        help = "Max depth for GET message (default is 1)"
    ).int().default(1)

    override fun createRequest(): Request {
        return Request(get_ = Get(param_paths = paths, max_depth = maxDepth))
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

    private val firstLevelOnly by option(
        "-f",
        "--first-level-only",
        help = "Request first level only"
    ).flag(default = false)

    private val skipCommands by option(
        "--skip-commands",
        help = "Do not return commands"
    ).flag(default = false)

    private val skipEvents by option(
        "--skip-events",
        help = "Do not return events"
    ).flag(default = false)

    private val skipParams by option(
        "--skip-params",
        help = "Do not return params"
    ).flag(default = false)

    override fun createRequest(): Request {
        return Request(
            get_supported_dm = GetSupportedDM(
                obj_paths = paths,
                first_level_only = firstLevelOnly,
                return_commands = !skipCommands,
                return_events = !skipEvents,
                return_params = !skipParams
            )
        )
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

    private val firstLevelOnly by option(
        "-f",
        "--first-level-only",
        help = "Request first level only"
    ).flag(default = false)

    override fun createRequest(): Request {
        return Request(
            get_instances = GetInstances(
                obj_paths = paths,
                first_level_only = firstLevelOnly,
            )
        )
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

    private val allowPartial by option(
        "-a",
        "--allow-partial",
        help = "Allow partial updates"
    ).flag(default = false)

    override fun createRequest(): Request {
        if (values.isEmpty() && required.isEmpty()) {
            throw PrintMessage("At least one parameter must be specified (using -p or -r)")
        }
        val params1 = values.map { v -> Set.UpdateParamSetting(v.key, v.value, false) }
        val params2 = required.map { v -> Set.UpdateParamSetting(v.key, v.value, true) }

        return Request(
            set_ = Set(
                update_objs = listOf(Set.UpdateObject(path, params1 + params2)),
                allow_partial = allowPartial
            )
        )
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

    private val allowPartial by option(
        "-a",
        "--allow-partial",
        help = "Allow partial adding"
    ).flag(default = false)

    override fun createRequest(): Request {
        if (values.isEmpty() && required.isEmpty()) {
            throw PrintMessage("At least one parameter must be specified (using -p or -r)")
        }
        val params1 = values.map { v -> Add.CreateParamSetting(v.key, v.value, false) }
        val params2 = required.map { v -> Add.CreateParamSetting(v.key, v.value, true) }

        return Request(
            add = Add(
                create_objs = listOf(Add.CreateObject(path, params1 + params2)),
                allow_partial = allowPartial
            )
        )
    }
}

abstract class RequestCommand(name: String, help: String) : CliktCommand(name = name, help = help) {

    var requestExecutor: ((Request) -> Unit)? = null

    override fun run() {
        val request = createRequest()
        requestExecutor?.let { it(request) }
    }

    abstract fun createRequest(): Request
}
