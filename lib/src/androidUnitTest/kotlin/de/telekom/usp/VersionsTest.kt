package de.telekom.usp

import org.junit.Test
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
        assertTrue(Versions.isSupported(Versions.mostRecent))
    }

    @Test
    fun `reject unsupported versions`() {
        listOf("1.1", "1.0", "", "xxx").forEach { version ->
            assertFalse(Versions.isSupported(version))
        }
    }
}