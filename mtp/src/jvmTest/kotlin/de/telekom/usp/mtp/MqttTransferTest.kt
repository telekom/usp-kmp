package de.telekom.usp.mtp

import de.telekom.usp.EndpointIdentifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import org.junit.Test
import socket.tls.TLSClientSettings
import kotlin.time.Duration.Companion.seconds

// This is in jvmTest purely for reading the password file, as there is not FileSystem.SYSTEM in
// the common source tree.
class MqttTransferTest {

    @Test
    fun `connection to server`() {
        val passwd = readPassword()
        val from = EndpointIdentifier("proto::usp-demo")
        val transfer = MqttTransfer(
            host = "home.kempmobil.de",
            port = 8883,
            user = "usp-demo",
            password = passwd,
            tls = TLSClientSettings(),
            from = from,
            subscribeTopics = mutableListOf("usp-demo-topic"),
            replyToTopic = "test"
        )
        GlobalScope.launch {
            transfer.events.collect {
                println("MQTT event: $it")
            }
        }
        runBlocking {
            println("Connecting...")
            transfer.connect()
            println("Connected, sending...")
            transfer.send("abc".encodeUtf8())
            delay(2.seconds)
            println("Disconnecting...")
            transfer.disconnect()
            println("Disconnected")
            delay(1.seconds)
        }
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
