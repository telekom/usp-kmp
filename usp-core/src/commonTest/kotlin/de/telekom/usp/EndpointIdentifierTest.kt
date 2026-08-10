/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class EndpointIdentifierTest {

    private val endpoints = listOf(
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

    @Test
    fun `parses correct endpoint identifiers`() {
        endpoints.forEach { text ->
            val endpoint = EndpointIdentifier(text)
            assertEquals(text, endpoint.toShortString())
            assertEquals("urn:bbf:usp:id:$text", endpoint.toLongString())
        }
    }

    @Test
    fun `parses correct endpoint identifiers with namespace`() {
        endpoints.forEach { text ->
            val endpoint = EndpointIdentifier("urn:bbf:usp:id:$text")
            assertEquals(text, endpoint.toShortString())
            assertEquals("urn:bbf:usp:id:$text", endpoint.toLongString())
        }
    }

    @Test
    fun `equals identifies same endpoints`() {
        endpoints.forEach { text ->
            assertEquals(EndpointIdentifier(text), EndpointIdentifier(text))
        }

        assertNotEquals(
            EndpointIdentifier("os::00256D-0123456789"),
            EndpointIdentifier("ops::00256D-0123456789")
        )
        assertNotEquals(
            EndpointIdentifier("cid:3AA3F8:my-unique-usp-id-42"),
            EndpointIdentifier("cid:3BB3F8:my-unique-usp-id-42")
        )
        assertNotEquals(
            EndpointIdentifier("cid:3AA3F8:my-unique-usp-id-42"),
            EndpointIdentifier("cid:3AA3F8:my-unique-usp-id-43")
        )
    }

    @Test
    fun `reject invalid endpoint identifiers`() {
        listOf(
            "",
            "  ",
            "os",
            "cid:twoparts",
            "cid:twoparts:",
            "illegal::test",
            "os:illegal:123",
            "os::illegal$$$"
        ).forEach {
            assertFailsWith<IllegalArgumentException> {
                EndpointIdentifier(it)
            }
        }
    }
}