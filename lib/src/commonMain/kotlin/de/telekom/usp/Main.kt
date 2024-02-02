package de.telekom.usp

import de.telekom.usp.messages.Get
import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.MessageConverterImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.ByteString.Companion.toByteString
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    val host = "127.0.0.1"
    val port = 5683
    val to = EndpointIdentifier("proto::AXACT")
    val from = EndpointIdentifier("proto::self")
    val messages: MessageConverter = MessageConverterImpl(from, to)
    val msg = Get { path("Device.DeviceInfo.") }
    val client = HttpClient {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
        }
        install(WebSockets)
    }

    val parser = GlobalScope.launch {
        messages.results.collect {
            println("---------- decoder result received: $it")
        }
    }

    suspend fun DefaultClientWebSocketSession.incomingMessages() {
        try {
            for (message in incoming) {
                val binary = incoming.receive() as? Frame.Binary ?: continue
                println("Received binary data of size: ${message.data.size}")
                messages.next(binary.readBytes().toByteString())
            }
        } catch (e: CancellationException) {
            // Ignore
        } catch (e: Exception) {
            println("Error while receiving: " + e::class)
        }
    }

    suspend fun DefaultClientWebSocketSession.sendMessages() {
        try {
            val bytes = messages.noSessionContextMessage(msg).toByteArray()
            println("Sending binary frame...")
            outgoing.send(Frame.Binary(fin = true, data = bytes))
            delay(1.seconds.inWholeMilliseconds)
        } catch (e: Exception) {
            println("Error while sending: " + e.message)
            return
        }
    }

    println("Connecting to $host:$port...")
    runBlocking {
        client.webSocket(
            method = HttpMethod.Get,
            host = host,
            port = port,
            path = "/endpointresource?eid=${from.toShortString()}",
            request = {
                headers[HttpHeaders.SecWebSocketProtocol] = "v1.usp"
                headers[HttpHeaders.SecWebSocketExtensions] = "bbf-usp-protocol"
            }
        ) {
            println("Connected!")
            val receiverRoutine = launch { incomingMessages() }
            val senderRoutine = launch { sendMessages() }

            senderRoutine.join()
            receiverRoutine.cancelAndJoin()
            parser.cancelAndJoin()
        }
    }
    client.close()
    println("Connection closed. Goodbye!")
}