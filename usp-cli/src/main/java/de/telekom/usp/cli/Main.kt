/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

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
import de.telekom.usp.e2e.e2eMessageExchange
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
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

    @OptIn(ExperimentalSerializationApi::class)
    private fun readConfig(configPath: Path): MessageTransferConfig {
        if (!FileSystem.SYSTEM.exists(configPath)) {
            throw FileNotFound("Missing configuration file: '$configPath'")
        }

        try {
            FileSystem.SYSTEM.source(configPath).buffer().use { source ->
                return Json.decodeFromBufferedSource<MessageTransferConfig>(source)
            }
        } catch (ex: Exception) {
            throw InvalidFileFormat(configPath.toString(), "${ex.message}")
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

        val messageExchange = with(readConfig(configPath)) {
            e2eMessageExchange(from, to) {
                debugMode = isDebugLevel || isVerboseLevel
                allowSessionContext = true

                whenMqtt { mqtt ->
                    mqttTransfer(mqtt.host, mqtt.port) {
                        user = mqtt.user
                        password = mqtt.password
                        useTls = mqtt.useTls
                    }
                }

                whenWebsocket { ws ->
                    wsTransfer(ws.host, ws.port) { }
                }
            }
        }

        // Pass the required data to the subcommand:
        currentContext.findOrSetObject { CommandContext(messageExchange, timeout.seconds) }
    }
}

fun main(args: Array<String>) = Main().subcommands(Commands).main(args)



