package de.telekom.usp

import kotlin.test.Test
import kotlin.test.assertEquals

class EndpointIdentifierTest {

    @Test
    fun `parses correct endpoint identifiers`() {
        val endpoints = listOf(
            "cid:3AA3F8:my-unique-usp-id-42",
            "pen:3561:my-unique-bbf-id-42",
            "self::my-Agent",
            "os::00256D-0123456789",
            "ops::00256D-STB-0123456789",
            "uuid::f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
            "imei::990000862471854",
            "proto::my-Agent",
            "fqdn::www.example.org"
        )

        for (text in endpoints) {
            val endpoint = EndpointIdentifier(text)
            assertEquals(text, endpoint.toShortString())
            assertEquals("urn:bbf:usp:id:$text", endpoint.toLongString())
        }
    }
}