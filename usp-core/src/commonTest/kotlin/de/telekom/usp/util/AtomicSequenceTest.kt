package de.telekom.usp.util

import kotlin.test.Test
import kotlin.test.assertEquals

class AtomicSequenceTest {

    @Test
    fun `increments integers`() {
        val int = AtomicSequence(1)
        assertEquals(2, int.next())
        assertEquals(3, int.next())
        assertEquals(4, int.next())
        assertEquals(5, int.next())
        assertEquals(6, int.next())
    }
}