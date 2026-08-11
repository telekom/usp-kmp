/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.mtp

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.mtp.mqtt.MqttConfig
import de.telekom.usp.mtp.mqtt.MqttTransfer
import de.telekom.usp.mtp.mqtt.Topic
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer

class MqttTransferTest {

    //@Test
    fun `connection to server`() = runTest {
        val passwd = readPassword()
        val from = EndpointIdentifier("proto::usp-demo")
        val to = EndpointIdentifier("proto::AXACT")
        val transfer = MqttTransfer(
            host = "home.kempmobil.de",
            port = 8883,
            user = "usp-demo",
            password = passwd,
            useTls = true,
            from = from,
            mqttConfig = MqttConfig(null, Topic(from.toString()), Topic(to.toString()))
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
