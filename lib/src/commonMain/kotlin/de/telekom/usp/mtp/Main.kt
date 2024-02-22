package de.telekom.usp.mtp

import co.touchlab.kermit.Logger
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.Reboot
import de.telekom.usp.messages.MessageConversionResult
import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.MessageConverterImpl
import de.telekom.usp.messages.dsl.Operate
import de.telekom.usp.messages.proto.debugMessage
import de.telekom.usp.messages.proto.getResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    val host = "127.0.0.1"
    val port = 5683
    val to = EndpointIdentifier("proto::AXACT")
    val from = EndpointIdentifier("proto::usp-demo")
    val messages: MessageConverter = MessageConverterImpl(from, to)
    val connection =
        WebSocketConnection(host, port, from, debugMode = true, pingDuration = 10.minutes)
//    val msg = Get {
//        this.maxDepth = 3
//        path(WiFi)
//    }
    val msg = Operate(Reboot, "key") { }

    val job1 = GlobalScope.launch {
        messages.results.collect {
            if (it is MessageConversionResult.Message) {
                val msg = it.msg
                Logger.d { "Received message of type ${msg.header_?.msg_type}" }
                println(msg.getResponse.debugMessage())
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
        messages.sessionContextMessage(msg).forEach { connection.send(it) }
        println("Waiting...")
        delay(5.seconds)
        println("Closing...")
        connection.disconnect()
        println("Closed.")

        job1.cancelAndJoin()
        job2.cancelAndJoin()
    }
}