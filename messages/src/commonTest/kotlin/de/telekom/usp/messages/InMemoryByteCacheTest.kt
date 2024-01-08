package de.telekom.usp.messages

import okio.ByteString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class InMemoryByteCacheTest {

    private val sample = ByteString.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    private lateinit var cache: InMemoryByteCache

    @BeforeTest
    fun setup() {
        cache = InMemoryByteCache()
    }

    @Test
    fun `set and get should return cached values`() {
        cache[1, 42] = sample
        cache[1, 43] = sample

        assertSame(sample, cache[1, 42])
        assertSame(sample, cache[1, 43])
        assertNull(cache[1, 1])
        assertNull(cache[1, 41])
        assertNull(cache[2, 42])
    }

    @Test
    fun `clears all entries`() {
        cache[1, 1] = sample
        cache[1, 2] = sample
        cache[3, 2] = sample
        cache.clearAll()

        assertNull(cache[1, 1])
        assertNull(cache[1, 2])
        assertNull(cache[3, 2])
    }

    @Test
    fun `clear all entries for specific session ID`() {
        cache[1, 1] = sample
        cache[1, 2] = sample
        cache[3, 1] = sample
        cache.clearAll(1)

        assertNull(cache[1, 1])
        assertNull(cache[1, 2])
        assertSame(sample, cache[3, 1])
    }

    @Test
    fun `clear all sequence ID below limit`() {
        cache[1, 1] = sample
        cache[1, 2] = sample
        cache[1, 3] = sample
        cache[1, 5] = sample
        cache.clearUpTo(1, 2)

        assertNull(cache[1, 1])
        assertNull(cache[1, 2])
        assertSame(sample, cache[1, 3])
        assertSame(sample, cache[1, 5])
    }
}