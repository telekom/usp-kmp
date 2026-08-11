/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.util

import kotlin.test.Test
import kotlin.test.assertEquals

class Fnv1aTest {

    @Test
    fun `fnv1a computation`() {
        val hash = "Device.DeviceInfo.".fnv1aHash()
        assertEquals(0xd64d94c9.toInt(), hash)
    }
}