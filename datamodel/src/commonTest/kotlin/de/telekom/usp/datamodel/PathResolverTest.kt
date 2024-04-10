package de.telekom.usp.datamodel

import de.telekom.usp.IP
import de.telekom.usp.Path
import de.telekom.usp.ResolvedPath
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PathResolverTest {

    private val dataModel = mock<DataModel>()

    private val resolver = PathResolver(dataModel)

    @Test
    fun `a resolved path is returned unchanged`() = runTest {
        val resolved = IP + "Interface.1."

        val actual = resolver.resolve(resolved)
        assertTrue(actual.size == 1)
        assertSame(actual[0], resolved)
    }

    @Test
    fun `wildcard search`() = runTest {
        val result = InstanceObjectBuilder()
            .add(IP + "Interface.1.")
            .add(IP + "Interface.2.")
            .build()
        everySuspend { dataModel.read(IP + "Interface.") } returns result

        val actual = resolver.resolve(Path("Device.IP.Interface.*.IPv6Address."))
        assertTrue(actual.size == 2)
        assertEquals(listOf(
            "Device.IP.Interface.1.IPv6Address.",
            "Device.IP.Interface.2.IPv6Address.",
        ), actual.map { it.toString() })
    }

    @Test
    fun `simple search`() = runTest {
        val result = InstanceObjectBuilder()
            .add("Device.IP.Interface.1.", mapOf("Type" to "Loopback"))
            .add("Device.IP.Interface.2.", mapOf("Type" to "Normal", "Status" to "Up"))
            .build()
        everySuspend { dataModel.read(IP + "Interface.") } returns result

        val actual = resolver.resolve(Path("Device.IP.Interface.[Type==\"Normal\"].Status"))
        assertTrue(actual.size == 1)
        assertEquals("Device.IP.Interface.2.Status", actual[0].toString())
    }
}

class InstanceObjectBuilder {

    private val instances = mutableListOf<InstanceObject>()

    fun add(path: String, rows: Map<String, String> = emptyMap()): InstanceObjectBuilder {
        instances.add(InstanceObject(ResolvedPath(path), rows))
        return this
    }

    fun add(path: ResolvedPath, rows: Map<String, String> = emptyMap()): InstanceObjectBuilder {
        instances.add(InstanceObject(path, rows))
        return this
    }

    fun build() = instances.toList()
}