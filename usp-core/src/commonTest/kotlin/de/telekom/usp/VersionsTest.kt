/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersionsTest {

    @Test
    fun `accept supported versions`() {
        listOf("1.3", "1.2").forEach { version ->
            assertTrue(Versions.isSupported(version))
        }
    }

    @Test
    fun `most recent version is supported`() {
        assertTrue(Versions.isSupported(Versions.MOST_RECENT))
    }

    @Test
    fun `reject unsupported versions`() {
        listOf("0.9", "", "xxx").forEach { version ->
            assertFalse(Versions.isSupported(version))
        }
    }
}