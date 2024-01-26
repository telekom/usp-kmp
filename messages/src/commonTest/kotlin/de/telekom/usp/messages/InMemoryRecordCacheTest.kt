package de.telekom.usp.messages

import de.telekom.usp.proto.record.SessionContextRecord
import okio.ByteString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class InMemoryRecordCacheTest {

    private val data = ByteString.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    private val payload = listOf(data)

    private lateinit var cache: InMemoryRecordCache

    @BeforeTest
    fun setup() {
        cache = InMemoryRecordCache()
    }

    @Test
    fun `set and get should return cached values`() {
        val sample1 = sampleOf(1, 42)
        val sample2 = sampleOf(1, 43)
        cache.put(sample1)
        cache.put(sample2)

        assertSame(sample1, cache.fetch(1, 42))
        assertSame(sample2, cache.fetch(1, 43))
        assertNull(cache.fetch(1, 1))
        assertNull(cache.fetch(1, 41))
        assertNull(cache.fetch(2, 42))
    }

    @Test
    fun `payload data is concatenated into buffered source`() {
        val data1 = ByteString.of(1, 2, 3, 4)
        val data2 = ByteString.of(5, 6, 7, 8)
        val data3 = ByteString.of(9, 10, 11, 12)
        cache.put(sampleOf(1, 42, listOf(data1)))
        cache.put(sampleOf(1, 43, listOf(data2, data3)))

        val source = cache.payloadToBufferedSource(1, 42L..43L)
        assertEquals(0L, source.indexOf(ByteString.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)))
    }

    @Test
    fun `clear a single sequence ID`() {
        cache.put(sampleOf(1, 42))
        cache.clear(1, 42)

        assertNull(cache.fetch(1, 42))

        // Check this doesn't throw an error
        cache.clear(22, 99)
    }

    @Test
    fun `clears all entries`() {
        cache.put(sampleOf(1, 1))
        cache.put(sampleOf(1, 2))
        cache.put(sampleOf(3, 2))
        cache.clearAll()

        assertNull(cache.fetch(1, 1))
        assertNull(cache.fetch(1, 2))
        assertNull(cache.fetch(3, 2))
    }

    @Test
    fun `clear all entries for specific session ID`() {
        val sample = sampleOf(3, 1)
        cache.put(sampleOf(1, 1))
        cache.put(sampleOf(1, 2))
        cache.put(sample)
        cache.clearAll(1)

        assertNull(cache.fetch(1, 1))
        assertNull(cache.fetch(1, 2))
        assertSame(sample, cache.fetch(3, 1))
    }

    @Test
    fun `clear all sequence ID below limit`() {
        val sample1 = sampleOf(1, 3)
        val sample2 = sampleOf(1, 5)
        cache.put(sampleOf(1, 1))
        cache.put(sampleOf(1, 2))
        cache.put(sample1)
        cache.put(sample2)
        cache.clearUpTo(1, 2)

        assertNull(cache.fetch(1, 1))
        assertNull(cache.fetch(1, 2))
        assertSame(sample1, cache.fetch(1, 3))
        assertSame(sample2, cache.fetch(1, 5))
    }

    private fun sampleOf(
        sessionId: Long,
        sequenceId: Long,
        payload: List<ByteString> = this.payload
    ): SessionContextRecord {
        return SessionContextRecord(
            session_id = sessionId,
            sequence_id = sequenceId,
            payload = payload
        )
    }
}