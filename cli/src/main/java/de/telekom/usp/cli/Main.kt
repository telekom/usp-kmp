package de.telekom.usp.cli

import co.touchlab.kermit.Logger
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.FileNotFound
import com.github.ajalt.clikt.core.InvalidFileFormat
import com.github.ajalt.clikt.core.subcommands
import de.telekom.usp.messages.proto.Msg
import de.telekom.usp.mtp.MessageTransferConfig
import de.telekom.usp.mtp.MessageTransferEvent
import de.telekom.usp.mtp.MessageTransferFactory
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use


class Main : CliktCommand(invokeWithoutSubcommand = true) {

    @OptIn(ExperimentalSerializationApi::class)
    private fun readConfig(): MessageTransferConfig {
        val config = "config.json".toPath()
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


    override fun run() {
        val transfer = MessageTransferFactory().create(readConfig())

        // The run method of the main command is run BEFORE the run method of child commands, hence
        // we cannot retrieve information from the subcommand but must pass a kind of handler to it:
        val subcommand = currentContext.invokedSubcommand
        if (subcommand is RequestCommand) {
            subcommand.requestExecutor = { request: Msg ->
                runBlocking {
                    val events = launch {
                        transfer.events.collect { event ->
                            when (event) {
                                is MessageTransferEvent.Connected -> Logger.i("Connected to ${event.to}")
                                is MessageTransferEvent.Disconnected -> {
                                    Logger.i { "Disconnected from ${event.from}" }
                                    cancel()
                                }

                                is MessageTransferEvent.BytesReceived -> {
                                    Logger.i { "Message of size ${event.bytes.size} received" }
                                }

                                is MessageTransferEvent.ConnectionFailed -> {
                                    Logger.e { "Cannot connect to ${event.from}" }
                                    cancel()
                                }
                            }
                        }
                    }
                    println("Executing $request...")
                    transfer.connect()
                    transfer.send(Msg.ADAPTER.encodeByteString(request))

                    events.join()
                    transfer.disconnect()
                }
            }
        }
    }
}

fun main(args: Array<String>) = Main().subcommands(commands).main(args)



