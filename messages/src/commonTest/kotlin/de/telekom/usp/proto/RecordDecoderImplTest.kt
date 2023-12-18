package de.telekom.usp.proto

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.MessageNotSupported
import de.telekom.usp.Versions
import de.telekom.usp.proto.msg.Header
import de.telekom.usp.proto.msg.Msg
import de.telekom.usp.proto.record.DisconnectRecord
import de.telekom.usp.proto.record.MQTTConnectRecord
import de.telekom.usp.proto.record.NoSessionContextRecord
import de.telekom.usp.proto.record.Record
import de.telekom.usp.proto.record.STOMPConnectRecord
import de.telekom.usp.proto.record.WebSocketConnectRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class RecordDecoderImplTest {

    private val from = "proto::test-from"
    private val to = "proto::test-to"

    private lateinit var decoder: RecordDecoderImpl

    @BeforeTest
    fun setup() {
        val ctx = SessionContext(EndpointIdentifier(from), EndpointIdentifier(to), 42L)
        decoder = RecordDecoderImpl(ctx)
    }

    @Test
    fun `handle invalid record data gracefully`() {
        listOf(
            ByteString.EMPTY,
            "some-invalid-proto-data".encodeUtf8()
        ).forEach { data ->
            runTest {
                launch {
                    assertTrue(decoder.results.first() is RecordDecoderResult.DecoderError)
                }
                launch {
                    decoder.next(data)
                }
            }
        }
    }

    @Test
    fun `reject record with invalid version`() = runTest {
        withResultOf(Record(version = "1.0")) {
            assertTrue(it is RecordDecoderResult.UspError)
            assertEquals(it.error, MessageNotSupported)
        }
    }

    @Test
    fun `ignore record with unknown endpoint`() = runTest {
        withResultOf(Record(version = Versions.mostRecent, to_id = "proto::incorrect")) {
            assertNull(it)
        }
    }

    @Test
    fun `decode no session context record`() = runTest {
        val payload = Msg.ADAPTER.encodeByteString(Msg(header_ = Header(msg_id = "test-hdr")))
        val noSession = Record(
            version = Versions.mostRecent,
            to_id = to,
            no_session_context = NoSessionContextRecord(payload)
        )
        withResultOf(noSession) {
            assertTrue(it is RecordDecoderResult.Message)
            assertNotNull(it.msg.header_)
            assertEquals(it.msg.header_!!.msg_id, "test-hdr")
        }
    }

    @Test
    fun `decode disconnect record`() = runTest {
        val disconnect = Record(
            version = Versions.mostRecent,
            to_id = to,
            disconnect = DisconnectRecord("XUIHOIUFDUTR", 42)
        )

        withResultOf(disconnect) {
            assertTrue(it is RecordDecoderResult.Disconnect)
            assertEquals(it.reason, "XUIHOIUFDUTR")
            assertEquals(it.reasonCode, 42)
        }
    }

    @Test
    fun `decode web socket record`() = runTest {
        val webSocket = Record(
            version = Versions.mostRecent,
            to_id = to,
            websocket_connect = WebSocketConnectRecord()
        )

        withResultOf(webSocket) {
            assertTrue(it is RecordDecoderResult.WebSocketConnect)
        }
    }

    @Test
    fun `decode MQTT record`() = runTest {
        val mqtt = Record(
            version = Versions.mostRecent,
            to_id = to,
            mqtt_connect = MQTTConnectRecord(MQTTConnectRecord.MQTTVersion.V3_1_1, "mqtt-topic")
        )

        withResultOf(mqtt) {
            assertTrue(it is RecordDecoderResult.MqttConnect)
            assertEquals(it.version, "V3_1_1")
            assertEquals(it.subscribedTopic, "mqtt-topic")
        }
    }

    @Test
    fun `decode Stomp record`() = runTest {
        val stomp = Record(
            version = Versions.mostRecent,
            to_id = to,
            stomp_connect = STOMPConnectRecord(STOMPConnectRecord.STOMPVersion.V1_2, "stomp-dest")
        )

        withResultOf(stomp) {
            assertTrue(it is RecordDecoderResult.StompConnect)
            assertEquals(it.version, "V1_2")
            assertEquals(it.subscribedDestination, "stomp-dest")
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.withResultOf(record: Record, asserter: (RecordDecoderResult?) -> Unit) {
        val resultCollectorJob = launch {
            asserter(decoder.results.firstOrNull())
        }
        launch {
            decoder.next(Record.ADAPTER.encodeByteString(record))
        }

        // Normally we could return here, but in case nothing is emitted from the flow, we want to
        // pass `null` to the 'asserter'. Hence first make sure the job is actually run, then cancel
        // it to get a result:
        runCurrent()
        resultCollectorJob.cancel()
    }
}