package de.telekom.usp

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InstanceIdTest {

    @Test
    fun `parses valid instance ids correctly`() {
        val valid = listOf(
            "my-Agent",
            ".-_",
            "%20-990000862471854",
            "f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
            "00256D-STB-%20",
            "00256D-STB-%AB",
            "00256D-STB-%20F",
        )
        valid.forEach { instanceId ->
            assertTrue(InstanceId.isValidId(instanceId))
        }
    }

    @Test
    fun `detects invalid instance ids`() {
        val invalid = listOf(
            "(my-id)",
            "my-id%",
            "my-id%F",
            "%A",
            "%zzzzzzz",
            "aaa-%Azzzzzzz",
        )
        invalid.forEach { instanceId ->
            assertFalse(InstanceId.isValidId(instanceId))
        }
    }
}