package de.telekom.usp.mtp

import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.Error
import de.telekom.usp.NoError
import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.MessageConverterImpl
import de.telekom.usp.messages.RecordDecoderResult
import de.telekom.usp.messages.dsl.Get
import de.telekom.usp.proto.msg.getResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.minutes

fun main(args: Array<String>) {
    val host = "127.0.0.1"
    val port = 5683
    val to = EndpointIdentifier("proto::AXACT")
    val from = EndpointIdentifier("proto::usp-demo")
    val messages: MessageConverter = MessageConverterImpl(from, to)
    val connection =
        WebSocketConnection(host, port, from, debugMode = true, pingDuration = 10.minutes)
    val get = Get {
        this.maxDepth = 1
        path("Device.DeviceInfo.")
    }

    val job1 = GlobalScope.launch {
        messages.results.collect {
            if (it is RecordDecoderResult.Message) {
                val msg = it.msg
                Logger.d { "Received message of type ${msg.header_?.msg_type}" }
                val getResp = msg.getResponse
                getResp.req_path_results.forEach { requestedResult ->
                    if (requestedResult.err_code != NoError.code) {
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
    val job2 = GlobalScope.launch {
        connection.events.collect { event ->
            if (event is ConnectionEvent.BytesReceived) {
                messages.next(event.bytes)
            }
        }
    }

    runBlocking {
        println("Connecting...")
        connection.connect()
        println("Sending...")
        messages.sessionContextMessage(msg = get).forEach { connection.send(it) }
        println("Waiting...")
        delay(5.minutes)
        println("Closing...")
        connection.disconnect()
        println("Closed.")

        job1.cancelAndJoin()
        job2.cancelAndJoin()
    }
}