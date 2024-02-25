package de.telekom.usp

import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.MessageConverterImpl
import de.telekom.usp.messages.dsl.Get
import de.telekom.usp.messages.proto.GetResp
import de.telekom.usp.messages.proto.debugMessage
import de.telekom.usp.mtp.WebSocketConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.minutes

fun main(args: Array<String>) {
    val host = "127.0.0.1"
    val port = 5683
    val to = EndpointIdentifier("proto::AXACT")
    val from = EndpointIdentifier("proto::usp-demo")
    val converter: MessageConverter = MessageConverterImpl(from, to)
    val connection =
        WebSocketConnection(host, port, from, debugMode = true, pingDuration = 10.minutes)
    val msg = Get {
        this.maxDepth = 1
        addPath(DeviceInfo)
    }
//    val msg = Operate(Reboot, "key") { }

    val handler = MessageExchange(converter, connection)
    runBlocking {
        handler.sendRequest(msg, { response: GetResp ->
            println(response.debugMessage())
        }) { error ->
            println("Error received: $error")
        }
        delay(2000)
    }
    println("Main terminated...")
}