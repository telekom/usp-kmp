package de.telekom.usp

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class PathTest {

    @Test
    fun `parses correct paths without error`() {
        listOf(
            "Device.",
            "Device.IP.Interface.[Type==\"Normal\"].Status",
            "Device.IP.Interface.[Type==\"Normal\"&&Stats.ErrorsSent>0].IPv4Address.[AddressingType==\"Static\"].IPAddress",
            "Device.IP.Interface.*.",
            "Device.IP.Interface.*.Type",
            "Device.WiFi.SSID.1.LowerLayers#1+.Name",
            "RelativeParameter"
        ).forEach { path ->
            assertEquals(path, Path(path).toString())
        }
    }

    @Test
    fun `parses search expressions as last path element correctly`() {
        listOf(
            "Device.USB.USBHosts.Host.*.Device.[DeviceClass==08]." to "DeviceClass==08",
            "Device.IP.Interface.[Alias==\"WAN\"]." to "Alias==\"WAN\""
        ).forEach { pair ->
            val path = Path(pair.first)
            assertTrue(path.last() is PathElement.Expression)
            assertEquals(path.lastAs<PathElement.Expression>().value, pair.second)
        }
    }

    @Test
    fun `returns proper expression components for search paths`() {
        val path =
            Path("Device.NAT.PortMapping.[RemoteHost==\"\"&&ExternalPort==0&&Protocol==\"TCP\"].")
        val components = path.lastAs<PathElement.Expression>().components
        assertEquals(listOf("RemoteHost==\"\"", "ExternalPort==0", "Protocol==\"TCP\""), components)
    }

    @Test
    fun `identifies an object as the last element`() {
        assertTrue(Path("Object.").last() is PathElement.Object)
        assertTrue(Path("Device.IP.Interface.Object.").last() is PathElement.Object)
    }

    @Test
    fun `identifies a parameter as the last element`() {
        assertTrue(Path("Parameter").last() is PathElement.Parameter)
        assertTrue(Path("Device.IP.Interface.Parameter").last() is PathElement.Parameter)
    }

    @Test
    fun `identifies a command as the last element`() {
        assertTrue(Path("Command()").last() is PathElement.Command)
        assertTrue(Path("Device.IP.Interface.Command()").last() is PathElement.Command)
    }

    @Test
    fun `identifies an event as the last element`() {
        assertTrue(Path("Event!").last() is PathElement.Event)
        assertTrue(Path("Device.IP.Interface.Event!").last() is PathElement.Event)
    }

    @Test
    fun `provides correct information about terminal paths`() {
        assertTrue(Path("Device.IP.Interface.Parameter").isTerminal)
        assertTrue(Path("Device.IP.Interface.Command()").isTerminal)
        assertTrue(Path("Device.IP.Interface.Event!").isTerminal)

        assertFalse(Path("Device.IP.Interface.").isTerminal)
        assertFalse(Path("Device.IP.Interface.*.").isTerminal)
        assertFalse(Path("Device.IP.Interface.[Type==\"Normal\"&&Stats.ErrorsSent>0].").isTerminal)
    }

    @Test
    fun `terminal path elements are only allowed in the end`() {
        listOf(
            "Device.Event().IP.Interface.",
            "Device.Command!.IP.Interface."
        ).forEach { path ->
            val message = assertFailsWith<IllegalArgumentException> { Path(path) }.message
            assertNotNull(message)
            assertContains(message, "must be the last element in a path")
        }
    }

    @Test
    fun `allows appending valid paths`() {
        val path = Path("Device.IP.")
        assertEquals("Device.IP.Interface.", (path + "Interface.").toString())
        assertEquals("Device.IP.Interface.Event!", (path + "Interface.Event!").toString())
    }

    @Test
    fun `matches startsWith correctly`() {
        val path = Path("Device.IP.Interface.")
        assertTrue(path.startsWith(Device))
        assertTrue(path.startsWith(IP))
        assertTrue(path.startsWith(Path("Device.IP.Interface.")))

        assertFalse(path.startsWith(Path("Device.IP.Interface.*.")))
        assertFalse(path.startsWith(Path("Device.NAT.")))
        assertFalse(path.startsWith(Path("Crap.")))

        assertTrue(Device.startsWithDevice())
        assertTrue(DeviceInfo.startsWithDevice())

        assertFalse(Path("Relative.").startsWithDevice())
    }

    @Test
    fun `resolved path property`() {
        val resolved = Path("Device.IP.Interface.")
        assertTrue(resolved.isResolved)

        val expression = Path("Device.IP.Interface.[Name==\"eth0\"].")
        assertFalse(expression.isResolved)
    }
}