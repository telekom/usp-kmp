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