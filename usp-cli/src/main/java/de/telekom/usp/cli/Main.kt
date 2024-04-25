package de.telekom.usp.cli

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import de.telekom.usp.e2e.MessageExchange
import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.MessageConverterImpl
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import kotlin.time.Duration.Companion.seconds

private val Commands = listOf(
    GetCommand(),
    AddCommand(),
    SetCommand(),
    DeleteCommand(),
    GetSupportedProtocolCommand(),
    GetSupportedDmCommand(),
    GetInstancesCommand(),
    OperateCommand()
)

class Main : CliktCommand(invokeWithoutSubcommand = true, printHelpOnEmptyArgs = true) {

    private val configFile by option("-c", help = "Agent config file (default config.json)").path(
        mustExist = false,
        canBeFile = true,
        canBeDir = false,
        mustBeReadable = true
    )

    private val configPath: Path
        get() = configFile?.toOkioPath() ?: "config.json".toPath()

    private val timeout by option(
        "-t", "--timeout", help = "Timeout in seconds to wait (default 10 seconds)"
    ).int().default(10)

    private val isDebugLevel by option(
        "-v", "--debug", help = "Print verbose log messages"
    ).flag()

    private val isVerboseLevel by option(
        "-vv", "--verbose", help = "Print more verbose log messages"
    ).flag()

    private val logWriter = object : LogWriter() {

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            println(message)
            if (throwable != null) {
                println(throwable.stackTraceToString())
            }
        }
    }


    private fun configureLogging() {
        if (isVerboseLevel) {
            Logger.setMinSeverity(Severity.Verbose)
        } else if (isDebugLevel) {
            Logger.setMinSeverity(Severity.Debug)
        } else {
            Logger.setMinSeverity(Severity.Info)
        }
        Logger.setLogWriters(logWriter)
    }

    override fun run() {
        configureLogging()
        val factory = JsonMessageTransferFactory(configPath, isDebugLevel || isVerboseLevel)
        val transfer = factory.create()
        val converter: MessageConverter =
            MessageConverterImpl(factory.localEndpoint, factory.remoteEndpoint)
        val exchange = MessageExchange(converter = converter, transfer = transfer)

        Logger.d { "Created $transfer" }

        // Pass the required data to the subcommand:
        currentContext.findOrSetObject { CommandContext(exchange, timeout.seconds) }
    }
}

fun main(args: Array<String>) = Main().subcommands(Commands).main(args)



