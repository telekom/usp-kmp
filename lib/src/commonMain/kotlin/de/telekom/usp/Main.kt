package de.telekom.usp

import co.touchlab.kermit.Logger
import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.MessageConverterImpl
import de.telekom.usp.messages.RecordDecoderResult
import de.telekom.usp.messages.dsl.Get
import de.telekom.usp.proto.msg.GetResp
import de.telekom.usp.proto.msg.bodyAs
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
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
    val from = EndpointIdentifier("proto::usp-demo")
    val messages: MessageConverter = MessageConverterImpl(from, to)
    val msg = Get {
        this.maxDepth = 1
        path("Device.DeviceInfo.")
    }
    val client = HttpClient {
        developmentMode = true
        install(Logging) {
            logger = io.ktor.client.plugins.logging.Logger.SIMPLE
            level = LogLevel.ALL
        }
        install(WebSockets)
    }

    val parser = GlobalScope.launch {
        messages.results.collect {
            if (it is RecordDecoderResult.Message) {
                val msg = it.msg
                Logger.d { "Received message of type ${msg.header_?.msg_type}" }
                val getResp = msg.bodyAs<GetResp>()
                getResp.req_path_results.forEach { requestedResult ->
                    if (requestedResult.err_code != NO_ERROR) {
                        println("------------- ${Error.from(requestedResult.err_code)} -------------")
                    } else {
                        requestedResult.resolved_path_results.forEach { pathResult ->
                            println("------------- ${pathResult.resolved_path} -------------")
                            pathResult.result_params.forEach { kv ->
                                println("${kv.key}=${kv.value}")
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun DefaultClientWebSocketSession.incomingMessages() {
        try {
            for (message in incoming) {
                val binary = message as? Frame.Binary ?: continue
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
            delay(1.seconds)
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