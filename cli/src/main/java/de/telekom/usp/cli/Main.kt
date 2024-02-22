package de.telekom.usp.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import de.telekom.usp.messages.proto.Request


class Main : CliktCommand(invokeWithoutSubcommand = true) {

    override fun run() {
        // The run method of the main command is run BEFORE the run method of child commands, hence
        // we cannot retrieve information from the subcommand but must pass a kind of handler to it:
        val subcommand = currentContext.invokedSubcommand
        if (subcommand is RequestCommand) {
            subcommand.requestExecutor = { request: Request ->
                println("Executing $request")
            }
        }
    }
}

fun main(args: Array<String>) = Main().subcommands(commands).main(args)



