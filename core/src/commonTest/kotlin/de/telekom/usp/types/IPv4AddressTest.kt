package de.telekom.usp.types

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IPv4AddressTest {

    @Test
    fun `validation of valid IPv4 addresses`() {
        listOf(
            "",
            "1.1.1.1",
            "0.0.0.0",
            "255.255.255.255"
        ).forEach { text ->
            val address = IPv4Address(text)
            assertTrue(address.isValid(), "'$text' is a valid IPv4 address")
        }
    }

    @Test
    fun `validation of invalid IPv4 addresses`() {
        listOf(
            "abc",
            "  ",
            "1.1.1",
            "f.f.f.f",
            ".0.0.0.0",
            "256.256.256.256"
        ).forEach { text ->
            val address = IPv4Address(text)
            assertFalse(address.isValid(), "'$text' is not a valid IPv4 address")
        }
    }
}