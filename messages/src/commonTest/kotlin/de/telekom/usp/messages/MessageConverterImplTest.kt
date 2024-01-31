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
import de.telekom.usp.proto.record.UDSConnectRecord
import de.telekom.usp.proto.record.WebSocketConnectRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertSame
import kotlin.test.assertTrue


class MessageConverterImplTest {

    private val local = "self::usp-controller"
    private val remote = "self::usp-agent"

    private lateinit var converter: MessageConverterImpl

    private var resultCount: Int = 0

    private var expectedResultCount: Int = -1

    @BeforeTest
    fun setup() {
        converter = MessageConverterImpl(EndpointIdentifier(local), EndpointIdentifier(remote))

        resultCount = 0
        expectedResultCount = -1
    }

    @Test
    fun `decode invalid record data gracefully`() {
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
    fun `reject record with invalid version`() = runDecoderTest {
        withResultOf(Record(version = "1.0")) {
            assertIs<RecordDecoderResult.UspError>(it)
            assertEquals(MessageNotSupported, it.error)
        }
    }

    @Test
    fun `ignore record with unknown endpoint`() = runDecoderTest {
        val invalid = Record(version = Versions.mostRecent, to_id = "self::unknown")
        withResultOf(invalid, expectResultCount = 0) { }
    }

    @Test
    fun `decode no session context record`() = runDecoderTest {
        val payload = Msg.ADAPTER.encodeByteString(Msg(header_ = Header(msg_id = "test-hdr")))
        val noSession = Record(
            version = Versions.mostRecent,
            to_id = remote,
            no_session_context = NoSessionContextRecord(payload)
        )

        withResultOf(noSession) {
            assertIs<RecordDecoderResult.Message>(it)
            assertNotNull(it.msg.header_)
            assertEquals("test-hdr", it.msg.header_!!.msg_id)
        }
    }

    @Test
    fun `decode disconnect record`() = runDecoderTest {
        val disconnect = Record(
            version = Versions.mostRecent,
            to_id = remote,
            disconnect = DisconnectRecord(CommandCanceled.name, CommandCanceled.code)
        )

        withResultOf(disconnect) {
            assertIs<RecordDecoderResult.Disconnect>(it)
            assertEquals(CommandCanceled.name, it.error.name)
            assertEquals(CommandCanceled.code, it.error.code)
        }
    }

    @Test
    fun `decode web socket record`() = runDecoderTest {
        val webSocket = Record(
            version = Versions.mostRecent,
            to_id = remote,
            websocket_connect = WebSocketConnectRecord()
        )

        withResultOf(webSocket) {
            assertIs<RecordDecoderResult.WebSocketConnect>(it)
        }
    }

    @Test
    fun `decode MQTT record`() = runDecoderTest {
        val mqtt = Record(
            version = Versions.mostRecent,
            to_id = remote,
            mqtt_connect = MQTTConnectRecord(MQTTConnectRecord.MQTTVersion.V3_1_1, "mqtt-topic")
        )

        withResultOf(mqtt) {
            assertIs<RecordDecoderResult.MqttConnect>(it)
            assertEquals("V3_1_1", it.version)
            assertEquals("mqtt-topic", it.subscribedTopic)
        }
    }

    @Test
    fun `decode Stomp record`() = runDecoderTest {
        val stomp = Record(
            version = Versions.mostRecent,
            to_id = remote,
            stomp_connect = STOMPConnectRecord(STOMPConnectRecord.STOMPVersion.V1_2, "stomp-dest")
        )

        withResultOf(stomp) {
            assertIs<RecordDecoderResult.StompConnect>(it)
            assertEquals("V1_2", it.version)
            assertEquals("stomp-dest", it.subscribedDestination)
        }
    }

    @Test
    fun `decode unix domain socket record`() = runDecoderTest {
        val udsSocket = Record(
            version = Versions.mostRecent,
            to_id = remote,
            uds_connect = UDSConnectRecord()
        )

        withResultOf(udsSocket) {
            assertIs<RecordDecoderResult.UdsConnect>(it)
        }
    }

    @Test
    fun `encode no session message`() {
        val bytes = converter.noSessionContextMessage(Msg(header_ = Header(msg_id = "test-header")))
        val record = Record.ADAPTER.decode(bytes)
        assertNotNull(record.no_session_context)
    }

    @Test
    fun `encode disconnect message`() {
        val bytes = converter.disconnect(MessageNotSupported)
        val record = Record.ADAPTER.decode(bytes)
        assertNotNull(record.disconnect)
        assertEquals(MessageNotSupported.code, record.disconnect!!.reason_code)
    }

    @Test
    fun `encode websocket connect`() {
        val bytes = converter.webSocketConnect()
        val record = Record.ADAPTER.decode(bytes)
        assertNotNull(record.websocket_connect)
    }

    @Test
    fun `encode UDS connect`() {
        val bytes = converter.udsConnect()
        val record = Record.ADAPTER.decode(bytes)
        assertNotNull(record.uds_connect)
    }

    @Test
    fun `encode MQTT connect`() {
        val bytes = converter.mqttConnect("5.0", "test-topic")
        val record = Record.ADAPTER.decode(bytes)
        assertNotNull(record.mqtt_connect)
        assertEquals("test-topic", record.mqtt_connect!!.subscribed_topic)
    }

    @Test
    fun `encode STOMP connect`() {
        val bytes = converter.stompConnect("1.2", "test-destination")
        val record = Record.ADAPTER.decode(bytes)
        assertNotNull(record.stomp_connect)
        assertEquals("test-destination", record.stomp_connect!!.subscribed_destination)
    }

    // -- Session Context Tests --------------------------------------------------------------------

    @Test
    fun `reject session creation when not allowed to`() = runDecoderTest {
        converter = MessageConverterImpl(
            EndpointIdentifier(local),
            EndpointIdentifier(remote),
            allowSessionContext = false
        )
        val start = asRecord(SessionContextRecord(session_id = 43L))

        withResultOf(start) {
            assertIs<RecordDecoderResult.UspError>(it)
            assertSame(SessionContextNotAllowed, it.error)
        }
    }

    @Test
    fun `start a new session when receiving an initial session context record`() = runDecoderTest {
        val start = asRecord(SessionContextRecord(session_id = 43L))

        withResultOf(start) {
            assertIs<RecordDecoderResult.SessionEstablished>(it)
            assertEquals(43L, it.sessionContext.sessionId)
            assertEquals(1L, it.sessionContext.sequenceId)
            assertFalse(it.isRestarted)
        }
    }

    @Test
    fun `restart the session when receiving a session context with a new session ID`() =
        runDecoderTest {
            val start = asRecord(SessionContextRecord(session_id = 42L))
            converter.next(Record.ADAPTER.encodeByteString(start))

            val restart = asRecord(SessionContextRecord(4711L))

            withResultOf(restart) {
                assertIs<RecordDecoderResult.SessionEstablished>(it)
                assertEquals(4711, it.sessionContext.sessionId)
                assertEquals(1L, it.sessionContext.sequenceId) // See R-E2E.6
                assertNotNull(it.previousSessionContext)
            assertTrue(it.isRestarted)
        }
    }

    @Test
    fun `emit retransmit request when present in record`() = runDecoderTest {
        val retransmit = asRecord(
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
    fun `parse single plain text session context record`() = runDecoderTest {
        val payload = Msg.ADAPTER.encodeByteString(Msg(header_ = Header(msg_id = "test-header")))
        val parts = payload.chunked(payload.size / 2)
        val msg =
            asRecord(SessionContextRecord(session_id = 43L, sequence_id = 1L, payload = parts))

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
    fun `parse several single plain text session context records out of order`() = runDecoderTest {
        listOf(1L, 4L, 2L, 3L).map { sequenceId ->
            val payload =
                Msg.ADAPTER.encodeByteString(Msg(header_ = Header(msg_id = "test-$sequenceId")))
            asRecord(
                SessionContextRecord(
                    session_id = 43L,
                    sequence_id = sequenceId,
                    payload = listOf(payload)
                )
            )
        }.forEach { record ->
            withResultOf(record, expectResultCount = 6) {
                when (resultCount) {
                    1 -> {
                        assertIs<RecordDecoderResult.SessionEstablished>(it)
                    }

                    2 -> {
                        assertIs<RecordDecoderResult.Message>(it)
                        assertEquals("test-1", it.msg.header_!!.msg_id)
                    }

                    3 -> {
                        assertIs<RecordDecoderResult.RecordsMissing>(it)
                    }

                    4, 5, 6 -> {
                        assertIs<RecordDecoderResult.Message>(it)
                        assertEquals("test-${resultCount - 2}", it.msg.header_!!.msg_id)
                    }
                }
            }
        }
    }

    @Test
    fun `parse multiple ordered plain text session context records`() = runDecoderTest {
        val payload = Msg.ADAPTER.encodeByteString(Msg(header_ = Header(msg_id = "test-header")))

        payload.chunked(payload.size / 3).asRecords().forEach { msg ->
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
    }

    @Test
    fun `parse multiple unordered plain text session context records`() = runDecoderTest {
        val payload = Msg.ADAPTER.encodeByteString(Msg(header_ = Header(msg_id = "test-header")))

        payload.chunked(payload.size / 3).asRecords().reversed().forEach { msg ->
            withResultOf(msg, expectResultCount = 4) {
                when (resultCount) {
                    1 -> {
                        assertIs<RecordDecoderResult.SessionEstablished>(it)
                    }

                    2, 3 -> {
                        assertIs<RecordDecoderResult.RecordsMissing>(it)
                    }

                    4 -> {
                        assertIs<RecordDecoderResult.Message>(it)
                        val header = it.msg.header_
                        assertNotNull(header)
                        assertEquals("test-header", header.msg_id)
                    }
                }
            }
        }
    }

    // -- Helper functions -------------------------------------------------------------------------

    private fun asRecord(sessionContext: SessionContextRecord): Record {
        return Record(
            version = Versions.mostRecent,
            to_id = remote,
            from_id = local,
            session_context = sessionContext
        )
    }

    private fun List<ByteString>.asRecords(): List<Record> {
        return mapIndexed { index, part ->
            val isFirst = index == 0
            val isLast = index == size - 1
            asRecord(
                SessionContextRecord(
                    session_id = 43L,
                    sequence_id = (index + 1).toLong(),
                    payload = listOf(part),
                    payload_sar_state = if (isFirst) BEGIN else if (isLast) COMPLETE else INPROCESS
                )
            )
        }
    }

    private fun runDecoderTest(testBody: suspend TestScope.() -> Unit) = runTest {
        testBody()
        assertEquals(expectedResultCount, resultCount)
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
            converter.results.collect {
                resultCount++
                asserter(it)
            }
        }
        launch {
            converter.next(data)
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