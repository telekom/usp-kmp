package de.telekom.usp.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import de.telekom.usp.proto.msg.Request


class Main : CliktCommand(invokeWithoutSubcommand = true) {

    override fun run() {
        val subcommand = currentContext.invokedSubcommand
        if (subcommand is RequestCommand) {
            subcommand.requestExecutor = { request: Request ->
                println("Executing $request")
            }
        }
    }
}

fun main(args: Array<String>) = Main().subcommands(commands).main(args)



