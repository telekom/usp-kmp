package de.telekom.usp

import kotlin.test.Test
import kotlin.test.assertEquals

class AuthorityIdTest {

    @Test
    fun `toString returns the original value`() {
        val authorityId = AuthorityId("abc")
        assertEquals("abc", authorityId.toString())
    }
}