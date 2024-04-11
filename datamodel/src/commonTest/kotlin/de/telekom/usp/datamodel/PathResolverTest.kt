package de.telekom.usp.datamodel

import de.telekom.usp.IP
import de.telekom.usp.Path
import de.telekom.usp.USB
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PathResolverTest {

    private val dataModel = mock<DataModel>()

    private val resolver = PathResolver(dataModel)

    @BeforeTest
    fun setup() {
        resetAnswers(dataModel)
    }

    @Test
    fun `a resolved path is returned unchanged`() = runTest {
        val query = IP + "Interface.1."
        val actual = resolver.resolve(query)

        assertTrue(actual.size == 1)
        assertSame(actual[0], query)
    }

    @Test
    fun `wildcard search with empty result`() = runTest {
        val query = Path("Device.IP.Interface.*.IPv6Address.ValidLifetime")

        everySuspend { dataModel.directChildren(IP + "Interface.") } returns emptyList()

        val actual = resolver.resolve(query)
        assertTrue(actual.isEmpty())
    }

    @Test
    fun `wildcard search`() = runTest {
        val query = Path("Device.IP.Interface.*.IPv6Address.ValidLifetime")

        everySuspend { dataModel.directChildren(IP + "Interface.") } returns listOf(
            IP + "Interface.1.",
            IP + "Interface.2."
        )

        val actual = resolver.resolve(query)
        assertEquals(
            listOf(
                IP + "Interface.1.IPv6Address.ValidLifetime",
                IP + "Interface.2.IPv6Address.ValidLifetime",
            ), actual
        )
    }

    @Test
    fun `basic expression search with empty result`() = runTest {
        val query = Path("Device.IP.Interface.[Type==\"Normal\"].Status")

        everySuspend { dataModel.directChildren(IP + "Interface.") } returns listOf(
            IP + "Interface.1.",
            IP + "Interface.2."
        )
        everySuspend { dataModel.readParameter(IP + "Interface.1.Type") } returns "Loopback"
        everySuspend { dataModel.readParameter(IP + "Interface.2.Type") } returns null

        val actual = resolver.resolve(query)
        assertTrue(actual.isEmpty())
    }

    @Test
    fun `basic expression search`() = runTest {
        val query = Path("Device.IP.Interface.[Type==\"Normal\"].Status")

        everySuspend { dataModel.directChildren(IP + "Interface.") } returns listOf(
            IP + "Interface.1.",
            IP + "Interface.2."
        )
        everySuspend { dataModel.readParameter(IP + "Interface.1.Type") } returns "Loopback"
        everySuspend { dataModel.readParameter(IP + "Interface.2.Type") } returns "Normal"

        val actual = resolver.resolve(query)
        assertTrue(actual.size == 1)
        assertEquals(IP + "Interface.2.Status", actual.first())
    }

    @Test
    fun `search with wildcard and expression`() = runTest {
        val query = Path("Device.USB.USBHosts.Host.*.Device.[DeviceClass==08].")

        everySuspend { dataModel.directChildren(USB + "USBHosts.Host.") } returns listOf(
            USB + "USBHosts.Host.1.",
            USB + "USBHosts.Host.2."
        )
        everySuspend { dataModel.directChildren(USB + "USBHosts.Host.1.Device.") } returns listOf(
            USB + "USBHosts.Host.1.Device.1.",
            USB + "USBHosts.Host.1.Device.2."
        )
        everySuspend { dataModel.directChildren(USB + "USBHosts.Host.2.Device.") } returns emptyList()
        everySuspend { dataModel.readParameter(USB + "USBHosts.Host.1.Device.1.DeviceClass") } returns "0"
        everySuspend { dataModel.readParameter(USB + "USBHosts.Host.1.Device.2.DeviceClass") } returns "8"

        val actual = resolver.resolve(query)
        assertTrue(actual.size == 1)
        assertEquals(USB + "USBHosts.Host.1.Device.2.", actual.first())
    }
}