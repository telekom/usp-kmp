package de.telekom.usp.messages

import de.telekom.usp.CommandCanceled
import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.MessageNotSupported
import de.telekom.usp.SessionContextNotAllowed
import de.telekom.usp.Versions
import de.telekom.usp.proto.msg.Header
import de.telekom.usp.proto.msg.Msg
import de.telekom.usp.proto.record.DisconnectRecord
import de.telekom.usp.proto.record.MQTTConnectRecord
import de.telekom.usp.proto.record.NoSessionContextRecord
import de.telekom.usp.proto.record.Record
import de.telekom.usp.proto.record.STOMPConnectRecord
import de.telekom.usp.proto.record.SessionContextRecord
import de.telekom.usp.proto.record.WebSocketConnectRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue


class RecordDecoderImplTest {

    private val from = "self::usp-controller"
    private val to = "self::usp-agent"

    private lateinit var decoder: RecordDecoderImpl

    @BeforeTest
    fun setup() {
        decoder = RecordDecoderImpl(EndpointIdentifier(to))
    }

    @Test
    fun `handle invalid record data gracefully`() {
        listOf(
            ByteString.EMPTY,
            "some-invalid-proto-data".encodeUtf8()
        ).forEach { data ->
            runTest {
                withResultOf(data) {
                    assertIs<RecordDecoderResult.DecoderError>(it)
                }
            }
        }
    }

    @Test
    fun `reject record with invalid version`() = runTest {
        withResultOf(Record(version = "1.0")) {
            assertIs<RecordDecoderResult.UspError>(it)
            assertEquals(MessageNotSupported, it.error)
        }
    }

    @Test
    fun `ignore record with unknown endpoint`() = runTest {
        withResultOf(Record(version = Versions.mostRecent, to_id = "self::unknown")) {
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
            assertIs<RecordDecoderResult.Message>(it)
            assertNotNull(it.msg.header_)
            assertEquals("test-hdr", it.msg.header_!!.msg_id)
        }
    }

    @Test
    fun `decode disconnect record`() = runTest {
        val disconnect = Record(
            version = Versions.mostRecent,
            to_id = to,
            disconnect = DisconnectRecord(CommandCanceled.name, CommandCanceled.code)
        )

        withResultOf(disconnect) {
            assertIs<RecordDecoderResult.Disconnect>(it)
            assertEquals(CommandCanceled.name, it.error.name)
            assertEquals(CommandCanceled.code, it.error.code)
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
            assertIs<RecordDecoderResult.WebSocketConnect>(it)
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
            assertIs<RecordDecoderResult.MqttConnect>(it)
            assertEquals("V3_1_1", it.version)
            assertEquals("mqtt-topic", it.subscribedTopic)
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
            assertIs<RecordDecoderResult.StompConnect>(it)
            assertEquals("V1_2", it.version)
            assertEquals("stomp-dest", it.subscribedDestination)
        }
    }

    // -- Session Context Tests --------------------------------------------------------------------

    @Test
    fun `reject session creation when not allowed to`() = runTest {
        decoder = RecordDecoderImpl(EndpointIdentifier(to), allowSessionContext = false)
        val start = Record(
            version = Versions.mostRecent,
            to_id = to,
            session_context = SessionContextRecord(43L)
        )
        withResultOf(start) {
            assertIs<RecordDecoderResult.UspError>(it)
            assertSame(SessionContextNotAllowed, it.error)
        }
    }

    @Test
    fun `start a new session when receiving an initial session context record`() = runTest {
        val start = Record(
            version = Versions.mostRecent,
            to_id = to,
            from_id = from,
            session_context = SessionContextRecord(43L)
        )
        withResultOf(start) {
            assertIs<RecordDecoderResult.SessionEstablished>(it)
            assertEquals(43L, it.sessionContext.sessionId)
            assertEquals(1L, it.sessionContext.sequenceId)
            assertFalse(it.isRestarted)
        }
    }

    @Test
    fun `restart the session when receiving a session context with a new session ID`() = runTest {
        val start = Record(
            version = Versions.mostRecent,
            to_id = to,
            from_id = from,
            session_context = SessionContextRecord(43L)
        )
        decoder.next(Record.ADAPTER.encodeByteString(start))

        val restart = Record(
            version = Versions.mostRecent,
            to_id = to,
            from_id = from,
            session_context = SessionContextRecord(4711L)
        )
        withResultOf(restart) {
            assertIs<RecordDecoderResult.SessionEstablished>(it)
            assertEquals(4711, it.sessionContext.sessionId)
            assertEquals(1L, it.sessionContext.sequenceId) // See R-E2E.6
            assertNotNull(it.previousSessionContext)
            assertTrue(it.isRestarted)
        }
    }


    private fun TestScope.withResultOf(record: Record, asserter: (RecordDecoderResult?) -> Unit) {
        withResultOf(Record.ADAPTER.encodeByteString(record), asserter)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.withResultOf(data: ByteString, asserter: (RecordDecoderResult?) -> Unit) {
        val resultCollectorJob = launch {
            asserter(decoder.results.firstOrNull())
        }
        launch {
            decoder.next(data)
        }

        // Normally we could return here, but in case nothing is emitted from the flow, we want to
        // pass `null` to the 'asserter'. Hence first make sure the job is actually run, then cancel
        // it to get a result:
        runCurrent()
        resultCollectorJob.cancel()
    }
}