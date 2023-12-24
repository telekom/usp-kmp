package de.telekom.usp.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import de.telekom.usp.proto.msg.Get
import de.telekom.usp.proto.msg.Request
import de.telekom.usp.proto.msg.Set

val commands = listOf(
    object : RequestCommand("get", "send GET message") {
        val path by option("-p", "--path", help = "Path to GET").multiple(required = true)
        val maxDepth by option("-m", "--max", help="May depth for GET message").int().default(1)

        override fun createRequest(): Request {
            return Request(get_ = Get(param_paths = path, max_depth = maxDepth))
        }
    },

    object : RequestCommand("set", "send SET message") {
        val path by option("-p", "--path", help = "Path to GET").multiple(required = true)

        override fun createRequest(): Request {
            path.map { path -> Set.UpdateObject() }
            return Request(set_ = Set(update_objs = emptyList()))
        }
    },
)

abstract class RequestCommand(name: String, help: String) : CliktCommand(name = name, help = help) {

    var requestExecutor: ((Request) -> Unit)? = null

    override fun run() {
        val request = createRequest()
        requestExecutor?.let { it(request) }
    }

    abstract fun createRequest(): Request
}