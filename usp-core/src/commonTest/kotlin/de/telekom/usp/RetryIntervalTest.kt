/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals


class RetryIntervalTest {

    @Test
    fun `intervals in expected ranges`() {
        val interval = RetryInterval("test")

        assertContains(5L..10L, interval.next().inWholeSeconds)
        assertContains(10L..20L, interval.next().inWholeSeconds)
        assertContains(20L..40L, interval.next().inWholeSeconds)
        assertContains(40L..80L, interval.next().inWholeSeconds)
        assertContains(80L..160L, interval.next().inWholeSeconds)
        assertContains(160L..320L, interval.next().inWholeSeconds)
        assertContains(320L..640L, interval.next().inWholeSeconds)
        assertContains(640L..1280L, interval.next().inWholeSeconds)
        assertContains(1280L..2560L, interval.next().inWholeSeconds)
        assertContains(2560L..5120L, interval.next().inWholeSeconds)

        assertEquals(5120L, interval.next().inWholeSeconds)
        assertEquals(5120L, interval.next().inWholeSeconds)
        assertEquals(5120L, interval.next().inWholeSeconds)
        assertEquals(5120L, interval.next().inWholeSeconds)
    }

    @Test
    fun `reset restarts the retry counter`() {
        val interval = RetryInterval("test")
        repeat(12) {
            interval.next()
        }
        interval.reset()
        assertContains(5L..10L, interval.next().inWholeSeconds)
    }
}