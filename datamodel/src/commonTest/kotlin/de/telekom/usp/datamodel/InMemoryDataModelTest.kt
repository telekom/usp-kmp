package de.telekom.usp.datamodel

import de.telekom.usp.Device
import de.telekom.usp.IP
import de.telekom.usp.WiFi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InMemoryDataModelTest {

    private val rows = mutableMapOf(
        "Enable" to "true",
        "Alias" to "eth0",
        "Status" to "Up",
        "Name" to "",
        "LastChange" to "0",
    )

    @Test
    fun `root path is always present`() = runTest {
        val model = InMemoryDataModel()
        assertTrue(model.read(Device).isNotEmpty())
    }

    @Test
    fun `read returns empty list for non existing paths`() = runTest {
        val model = InMemoryDataModel()
        model.add(InstanceObject(IP, rows))

        assertTrue(model.read(WiFi).isEmpty())
    }

    @Test
    fun `add stores values`() = runTest {
        val model = InMemoryDataModel()
        model.add(InstanceObject(IP, rows))

        val result = model.read(IP)
        assertTrue(result.size == 1)
        assertEquals(IP, result[0].path)
        assertEquals(rows, result[0].rows)
    }

    @Test
    fun `set replaces existing values`() = runTest {
        val model = InMemoryDataModel()
        val expected = mutableMapOf("abc" to "123", "def" to "true")
        model.add(InstanceObject(IP, rows))
        model.set(InstanceObject(IP, expected))

        val result = model.read(IP)
        assertTrue(result.size == 1)
        assertEquals(IP, result[0].path)
        assertEquals(expected, result[0].rows)
    }

    @Test
    fun `delete removes path from data model`() = runTest {
        val model = InMemoryDataModel()
        model.add(InstanceObject(IP, rows))
        model.add(InstanceObject(IP + "Interface.", rows))
        model.add(InstanceObject(IP + "Interface.1.", rows))

        assertTrue(model.delete(IP + "Interface.1."))
        assertTrue(model.read(IP + "Interface.1.", Int.MAX_VALUE).isEmpty())
        assertTrue(model.read(IP, Int.MAX_VALUE).size == 2)
    }

    @Test
    fun `deleting unknown paths returns false`() = runTest {
        val model = InMemoryDataModel()
        model.add(InstanceObject(IP, rows))

        assertFalse(model.delete(WiFi))
    }

    @Test
    fun `deleting the root path is not possible`() = runTest {
        val model = InMemoryDataModel()
        model.add(InstanceObject(IP, rows))

        assertFalse(model.delete(Device))
        assertTrue(model.read(IP).isNotEmpty())
    }

    @Test
    fun `directChildren returns exactly the direct children`() = runTest {
        val model = InMemoryDataModel()
        model.set(
            InstanceObject(IP + "Interface.1.", rows),
            InstanceObject(IP + "Interface.2.", rows),
            InstanceObject(IP + "Interface.1.IPv4Address.", rows),
        )

        val children = model.directChildren(IP + "Interface.")
        assertEquals(listOf(IP + "Interface.1.", IP + "Interface.2."), children)
    }

    @Test
    fun `read traverses the tree properly`() = runTest {
        val model = InMemoryDataModel()
        model.set(
            InstanceObject(IP + "Interface.1.", rows),
            InstanceObject(IP + "Interface.2.", rows),
            InstanceObject(IP + "Interface.1.IPv4Address.", rows),
        )

        assertTrue(model.read(IP, 0).size == 1)
        assertTrue(model.read(IP, 1).size == 2)
        assertTrue(model.read(IP, 2).size == 4)
        assertTrue(model.read(IP, Int.MAX_VALUE).size == 5)

        // Check the right rows are returned for every InstanceObject
        model.read(IP, Int.MAX_VALUE).forEach { instance ->
            when (instance.path) {
                IP, IP + "Interface." -> assertTrue(instance.rows.isEmpty())
                else -> assertEquals(instance.rows, rows)
            }
        }
    }
}