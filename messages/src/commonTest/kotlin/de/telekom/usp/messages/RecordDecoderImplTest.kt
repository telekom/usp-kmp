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
import de.telekom.usp.proto.record.SessionContextRecord.PayloadSARState.BEGIN
import de.telekom.usp.proto.record.SessionContextRecord.PayloadSARState.COMPLETE
import de.telekom.usp.proto.record.SessionContextRecord.PayloadSARState.INPROCESS
import de.telekom.usp.proto.record.WebSocketConnectRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue


class RecordDecoderImplTest {

    private val from = "self::usp-controller"
    private val to = "self::usp-agent"

    private lateinit var decoder: RecordDecoderImpl

    private var resultCount: Int = 0

    private var expectedResultCount: Int = -1

    @BeforeTest
    fun setup() {
        decoder = RecordDecoderImpl(EndpointIdentifier(to))

        resultCount = 0
        expectedResultCount = -1
    }

    @AfterTest
    fun assertResultCount() {
        assertEquals(expectedResultCount, resultCount)
    }

    @Test
    fun `handle invalid record data gracefully`() {
        listOf(
            ByteString.EMPTY,
            "some-invalid-proto-data".encodeUtf8()
        ).forEach { data ->
            runTest {
                withResultOf(data, 2) {
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
        val invalid = Record(version = Versions.mostRecent, to_id = "self::unknown")
        withResultOf(invalid, expectResultCount = 0) { }
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
        val start = recordWith(SessionContextRecord(session_id = 43L))

        withResultOf(start) {
            assertIs<RecordDecoderResult.UspError>(it)
            assertSame(SessionContextNotAllowed, it.error)
        }
    }

    @Test
    fun `start a new session when receiving an initial session context record`() = runTest {
        val start = recordWith(SessionContextRecord(session_id = 43L))

        withResultOf(start) {
            assertIs<RecordDecoderResult.SessionEstablished>(it)
            assertEquals(43L, it.sessionContext.sessionId)
            assertEquals(1L, it.sessionContext.sequenceId)
            assertFalse(it.isRestarted)
        }
    }

    @Test
    fun `restart the session when receiving a session context with a new session ID`() = runTest {
        val start = recordWith(SessionContextRecord(session_id = 42L))
        decoder.next(Record.ADAPTER.encodeByteString(start))

        val restart = recordWith(SessionContextRecord(4711L))

        withResultOf(restart) {
            assertIs<RecordDecoderResult.SessionEstablished>(it)
            assertEquals(4711, it.sessionContext.sessionId)
            assertEquals(1L, it.sessionContext.sequenceId) // See R-E2E.6
            assertNotNull(it.previousSessionContext)
            assertTrue(it.isRestarted)
        }
    }

    @Test
    fun `emit retransmit request when present in record`() = runTest {
        val retransmit = recordWith(
            SessionContextRecord(session_id = 43L, sequence_id = 1L, retransmit_id = 8000L)
        )

        withResultOf(retransmit, expectResultCount = 2) {
            when (resultCount) {
                1 -> {
                    assertIs<RecordDecoderResult.SessionEstablished>(it)
                }

                2 -> {
                    assertIs<RecordDecoderResult.Retransmit>(it)
                    assertEquals(8000L, it.sequenceId)
                }
            }
        }
    }

    @Test
    fun `parse single record in plain text session context`() = runTest {
        val payload = Msg.ADAPTER.encodeByteString(Msg(header_ = Header(msg_id = "test-header")))
        val parts = payload.chunked(payload.size / 2)
        val msg =
            recordWith(SessionContextRecord(session_id = 43L, sequence_id = 1L, payload = parts))

        withResultOf(msg, expectResultCount = 2) {
            when (resultCount) {
                1 -> {
                    assertIs<RecordDecoderResult.SessionEstablished>(it)
                }

                2 -> {
                    assertIs<RecordDecoderResult.Message>(it)
                    val header = it.msg.header_
                    assertNotNull(header)
                    assertEquals("test-header", header.msg_id)
                }
            }
        }
    }

    @Test
    fun `parse multiple records in order in plain text session context`() = runTest {
        val payload = Msg.ADAPTER.encodeByteString(Msg(header_ = Header(msg_id = "test-header")))
        val parts = payload.chunked(payload.size / 3)

        parts.forEachIndexed { index, part ->
            val isFirst = index == 0
            val isLast = index == parts.size - 1
            val msg = recordWith(
                SessionContextRecord(
                    session_id = 43L,
                    sequence_id = (index + 1).toLong(),
                    payload = listOf(part),
                    payload_sar_state = if (isFirst) BEGIN else if (isLast) COMPLETE else INPROCESS
                )
            )

            if (!isLast) {
                decoder.next(Record.ADAPTER.encodeByteString(msg))
            } else {
                withResultOf(msg) {
                    assertIs<RecordDecoderResult.Message>(it)
                    val header = it.msg.header_
                    assertNotNull(header)
                    assertEquals("test-header", header.msg_id)
                }
            }
        }
    }

    // -- Helper functions -------------------------------------------------------------------------

    private fun recordWith(sessionContext: SessionContextRecord): Record {
        return Record(
            version = Versions.mostRecent,
            to_id = to,
            from_id = from,
            session_context = sessionContext
        )
    }

    private fun TestScope.withResultOf(
        record: Record,
        expectResultCount: Int = 1,
        asserter: (RecordDecoderResult) -> Unit
    ) {
        withResultOf(Record.ADAPTER.encodeByteString(record), expectResultCount, asserter)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.withResultOf(
        data: ByteString,
        expectResultCount: Int = 1,
        asserter: (RecordDecoderResult) -> Unit
    ) {
        expectedResultCount = expectResultCount

        val job = launch {
            decoder.results.collect {
                resultCount++
                asserter(it)
            }
        }
        launch {
            decoder.next(data)
        }

        runCurrent()
        job.cancel()
    }

    /**
     * Convert the byte string into a list of byte strings, where each byte string is of length
     * `size`, plus the remaining byte string. When concatenating the resulting byte string it will
     * be equal to the input.
     */
    private fun ByteString.chunked(size: Int): List<ByteString> {
        return toByteArray().asList().windowed(size, size, partialWindows = true)
            .map { ByteString.of(*it.toByteArray()) }
    }
}