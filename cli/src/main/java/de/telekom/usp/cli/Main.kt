package de.telekom.usp.cli

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.FileNotFound
import com.github.ajalt.clikt.core.InvalidFileFormat
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import de.telekom.usp.MessageExchange
import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.MessageConverterImpl
import de.telekom.usp.mtp.MessageTransfer
import de.telekom.usp.mtp.MessageTransferConfig
import de.telekom.usp.mtp.MessageTransferEvent
import de.telekom.usp.mtp.MessageTransferFactory
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.buffer
import okio.use


class Main : CliktCommand(invokeWithoutSubcommand = true) {

    private val configFile by option("-c").path(
        mustExist = false,
        canBeFile = true,
        canBeDir = false,
        mustBeReadable = true
    )

    private val timeout by option(
        "-t", "--timeout", help = "Timeout in seconds to wait for a response"
    ).int().default(5)

    private val isDebugLevel by option(
        "-v", "--debug", help = "Print more verbose log messages"
    ).flag()

    private val isVerboseLevel by option(
        "-vv", "--verbose", help = "Print even more verbose log messages"
    ).flag()

    private val logWriter = object : LogWriter() {

        private val started by lazy { System.currentTimeMillis() }

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            val time = (System.currentTimeMillis() - started).coerceAtLeast(0L)
            // Omit the severity and the tag for brevity
            println("%06d".format(time) + " " + message)
            throwable?.printStackTrace()
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

    @OptIn(ExperimentalSerializationApi::class)
    private fun readConfig(): MessageTransferConfig {
        val config = if (configFile != null) {
            configFile!!.toOkioPath()
        } else {
            "config.json".toPath()
        }
        if (!FileSystem.SYSTEM.exists(config)) {
            throw FileNotFound("Missing configuration file: '$config'")
        }

        try {
            FileSystem.SYSTEM.source(config).buffer().use { source ->
                return Json.decodeFromBufferedSource<MessageTransferConfig>(source)
            }
        } catch (ex: Exception) {
            throw InvalidFileFormat(config.toString(), "${ex.message}")
        }
    }

    private suspend fun messageTransferEventCollector(transfer: MessageTransfer) {
        coroutineScope {
            transfer.events.collect { event ->
                when (event) {
                    is MessageTransferEvent.Connected -> {
                        Logger.i("Connected via ${event.to}")
                    }

                    is MessageTransferEvent.Disconnected -> {
                        Logger.d { "Disconnected from ${event.from}" }
                        cancel()
                    }

                    is MessageTransferEvent.BytesReceived -> {
                        Logger.d { "Message of size ${event.bytes.size} received" }
                    }

                    is MessageTransferEvent.ConnectionFailed -> {
                        Logger.e { "Cannot connect to ${event.from}" }
                        cancel()
                    }
                }
            }
        }
    }

    override fun run() {
        configureLogging()
        val config = readConfig()
        val transfer = MessageTransferFactory().create(config, isDebugLevel || isVerboseLevel)
        val converter: MessageConverter =
            MessageConverterImpl(config.localEndpoint, config.remoteEndpoint)
        val exchange = MessageExchange(converter = converter, transfer = transfer)

        Logger.d { "Created $transfer" }

        when (val subcommand = currentContext.invokedSubcommand) {
            is UspCommand -> {
                subcommand.exchange = exchange
            }

            else -> Logger.e { "Unexpected subcommand: $subcommand" }
        }

        // Next the subcommand's run() method is called by clikt
    }
}

fun main(args: Array<String>) = Main().subcommands(commands).main(args)



