package de.telekom.usp.mtp

import de.telekom.usp.EndpointIdentifier
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import socket.tls.TLSClientSettings
import kotlin.test.Test

// This is in jvmTest purely for reading the password file, as there is not FileSystem.SYSTEM in
// the common source tree.
class MqttTransferTest {

    @Test
    fun `connection to server`() = runTest {
        val passwd = readPassword()
        val from = EndpointIdentifier("proto::usp-demo")
        val transfer = MqttTransfer(
            host = "home.kempmobil.de",
            port = 8883,
            user = "usp-demo",
            password = passwd,
            tls = TLSClientSettings(),
            from = from,
            subscribeTopics = mutableListOf("usp-demo-topic2"),
            replyToTopic = "test"
        )
        val eventCollector = launch {
            transfer.events.collect {
                println("MQTT event: $it")
                cancel()
            }
        }

        runBlocking {
            println("Connecting...")
            transfer.connect()
            println("Connected, sending...")
            transfer.send("abc".encodeUtf8())
            println("Disconnecting...")
            transfer.disconnect()
            println("Disconnected")
        }
        eventCollector.join()
    }

    private fun readPassword(): String {
        println("Trying to read MQTT server password from file './../mqtt-passwd'...")
        val passwdFile = FileSystem.SYSTEM.canonicalize("./../mqtt-passwd".toPath())
        return FileSystem.SYSTEM.source(passwdFile).use { fileSource ->
            fileSource.buffer().use { bufferedFileSource ->
                bufferedFileSource.readUtf8Line()
            }
        }!!
    }
}
