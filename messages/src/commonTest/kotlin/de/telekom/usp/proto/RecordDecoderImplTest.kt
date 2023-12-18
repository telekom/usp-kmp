package de.telekom.usp.proto

import de.telekom.usp.EndpointIdentifier
import de.telekom.usp.MessageNotSupported
import de.telekom.usp.proto.record.Record
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


class RecordDecoderImplTest {

    private lateinit var decoder: RecordDecoderImpl

    @BeforeTest
    fun setup() {
        val context = SessionContext(
            from = EndpointIdentifier("proto::test-from"),
            to = EndpointIdentifier("proto::test-to"),
            sessionId = 42L)
        decoder = RecordDecoderImpl(context)
    }

    @Test
    fun `reject record with invalid version`() = runTest {
        withResultOf(Record(version = "1.0")) {
            assertTrue(it is RecordDecoderResult.Error)
            assertEquals(it.error, MessageNotSupported)
        }
    }

    @Test
    fun `ignore record with unknown endpoint`() = runTest {
        withResultOf(Record(version = "1.3", to_id = "proto::incorrect")) {
            assertNull(it)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.withResultOf(record: Record, asserter: (RecordDecoderResult?) -> Unit) {
        val resultCollectorJob = launch {
            asserter(decoder.result.firstOrNull())
        }
        launch {
            decoder.next(Record.ADAPTER.encodeByteString(record))
        }

        // Normally we could return here, but in case nothing is emitted from the shared flow, we
        // want to pass `null` to the 'asserter'. Hence first make sure the job is actually run,
        // then cancel it:
        runCurrent()
        resultCollectorJob.cancel()
    }
}