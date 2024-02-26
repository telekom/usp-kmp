package de.telekom.usp

import de.telekom.usp.messages.MessageConverter
import de.telekom.usp.messages.MessageConverterImpl
import de.telekom.usp.messages.dsl.Add
import de.telekom.usp.messages.dsl.Get
import de.telekom.usp.messages.dsl.required
import de.telekom.usp.messages.proto.AddResp
import de.telekom.usp.messages.proto.GetResp
import de.telekom.usp.messages.proto.debugMessage
import de.telekom.usp.mtp.WebSocketTransfer
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
        WebSocketTransfer(host, port, from, debugMode = true, pingDuration = 10.minutes)
    val get1 = Get {
        this.maxDepth = 2
        addPath(Device)
    }
    val addWifi = Add {
        allowPartial = false
        addPath(WiFi + "SSID.") {
            params["LowerLayers"] = "Device.WiFi.Radio.1." required true
            params["SSID"] = "My-New-WiFi" required true
        }
    }
    val errorHandler: (MessageExchangeFailure) -> Unit = { println("Error received: $it") }
    val exchange = MessageExchange(converter, connection)

    exchange.start()
    runBlocking {
        exchange.sendRequest(get1, errorHandler) { response: GetResp ->
            println(response.debugMessage())
        }
        delay(1000)
        exchange.sendRequest(addWifi, errorHandler) { response: AddResp ->
            println(response)
        }
        delay(1000)
        exchange.stop()
        delay(500)
    }
    println("Main terminated...")
}