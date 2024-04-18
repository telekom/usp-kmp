package de.telekom.usp.cli

import co.touchlab.kermit.Logger
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import de.telekom.usp.MessageExchange
import de.telekom.usp.MessageExchangeFailure
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock

/**
 * Base class for all sub commands.
 */
abstract class AbstractCommand(name: String, help: String) :
    CliktCommand(name = name, help = help) {

    private val context by requireObject<CommandContext>()

    private val barrier = MutableSharedFlow<Unit>()

    /**
     * Provides a default error handler for subclasses when sending requests via [MessageExchange].
     */
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

        onFinished()
    }

    override fun run() {
        val exchange = context.exchange
        val timeout = context.timeout
        val start = Clock.System.now()

        runBlocking {
            exchange.start()
            sendRequest(exchange)
            val success = withTimeoutOrNull(timeout) {
                barrier.first() // Waits for onFinished to be called
                true
            }

            exchange.stop()
            if (success == null) {
                Logger.w { "Timeout occurred while waiting for a response" }
            }
        }

        val duration = Clock.System.now() - start
        Logger.i { "Executed request in ${duration.inWholeMilliseconds}ms" }
    }

    /**
     * Must be called by implementing classes after processing the request has been finished.
     */
    protected fun onFinished() {
        runBlocking {
            barrier.emit(Unit)
        }
    }

    /**
     * Sends the actual request message via one of the `sendRequest` methods of the specified
     * exchange and prints the results. Must call [onFinished] when a result has been received
     */
    abstract suspend fun sendRequest(exchange: MessageExchange)
}
