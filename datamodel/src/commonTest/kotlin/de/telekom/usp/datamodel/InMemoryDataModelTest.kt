package de.telekom.usp.datamodel

import de.telekom.usp.ResolvedPath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class InMemoryDataModelTest {

    private val rows = mutableMapOf(
        "Enable" to "true",
        "Alias" to "eth0",
        "Status" to "Up",
        "Name" to "",
        "LastChange" to "0",
    )

    @Test
    fun `set inserts values`() = runTest {
        val model = InMemoryDataModel()
        model.set(
            InstanceObject(ResolvedPath("Device.IP.Interface.1."), rows),
            InstanceObject(ResolvedPath("Device.IP.Interface.2."), rows),
            InstanceObject(ResolvedPath("Device.IP.Interface.1.IPv4Address."), rows),
        )
    }
}