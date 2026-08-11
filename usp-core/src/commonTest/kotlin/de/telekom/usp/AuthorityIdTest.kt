/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

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