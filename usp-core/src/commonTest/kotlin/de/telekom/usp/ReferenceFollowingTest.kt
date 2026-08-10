/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReferenceFollowingTest {

    @Test
    fun `create item 1 reference following`() {
        listOf(
            "ReferenceParameter#1+", "ReferenceParameter+"
        ).forEach {
            val ref = ReferenceFollowing.from(it)
            assertNotNull(ref)
            assertEquals("ReferenceParameter", ref.name)
            assertEquals(1, ref.itemNumber)
        }
    }

    @Test
    fun `create all items reference following`() {
        val ref = ReferenceFollowing.from("ReferenceParameter#*+")
        assertNotNull(ref)
        assertEquals("ReferenceParameter", ref.name)
        assertEquals(0, ref.itemNumber)
    }

    @Test
    fun `create item number reference following`() {
        val ref = ReferenceFollowing.from("ReferenceParameter#12+")
        assertNotNull(ref)
        assertEquals("ReferenceParameter", ref.name)
        assertEquals(12, ref.itemNumber)
    }

    @Test
    fun `fail gracefully for illegal reference following`() {
        listOf(
            "", "abc", "#", "+", "*", "#12+",
        ).forEach {
            assertNull(ReferenceFollowing.from(it))
        }
    }
}