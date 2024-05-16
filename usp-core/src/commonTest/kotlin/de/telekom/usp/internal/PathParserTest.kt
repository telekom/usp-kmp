package de.telekom.usp.internal

import de.telekom.usp.Device
import de.telekom.usp.DeviceInfo
import de.telekom.usp.IP
import de.telekom.usp.Path
import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath
import de.telekom.usp.WiFi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class PathParserTest {

    @Test
    fun `parses correct paths without error`() {
        listOf(
            "Device.",
            "Device.IP.Interface.[Type==\"Normal\"].Status",
            "Device.IP.Interface.[Type==\"Normal\"&&Stats.ErrorsSent>0].IPv4Address.[AddressingType==\"Static\"].IPAddress",
            "Device.IP.Interface.*.",
            "Device.IP.Interface.*.Type",
            "Device.WiFi.SSID.1.LowerLayers+.Name",
            "RelativeParameter"
        ).forEach { path ->
            assertEquals(path, Path(path).toString())
        }
    }

    @Test
    fun `equals matches paths correctly`() {
        assertEquals(Path("Device.IP.Interface.*."), Path("Device.IP.Interface.*."))
        assertEquals(Path("Parameter"), Path("Parameter"))
        assertEquals(Path("Device.Reboot()"), Path("Device.Reboot()"))
        assertEquals(IP + "Interface.", IP + "Interface.")

        assertNotEquals(Path("Parameter."), Path("Parameter"))
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
    fun `parses reference following correctly`() {
        val path = Path("Device.NAT.PortMapping.1.Interface+.Name")
        assertTrue(path.elements.size == 6)
        val ref = path.elements[4]
        assertIs<PathElement.Object>(ref)
        assertNotNull(ref.refFollow)
        assertEquals(1, ref.refFollow!!.itemNumber)
        assertEquals("Interface", ref.refFollow!!.name)
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
        assertEquals("Device.IP.Interface.", (IP + "Interface.").toString())
        assertEquals("Device.IP.Interface.Event!", (IP + "Interface.Event!").toString())
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
    fun `matches endsWith correctly`() {
        val path = Path("Device.IP.Interface.")
        assertTrue(path.endsWith(Path("Interface.")))
        assertTrue(path.endsWith(Path("IP.Interface.")))
        assertTrue(path.endsWith(Path("Device.IP.Interface.")))

        assertFalse(path.endsWith(WiFi))
    }

    @Test
    fun `auto-detect resolved paths`() {
        listOf(
            "Device.IP.Interface.",
            "Device.IP.Interface.Status",
            "Device.IP.Interface.1.",
            "Device.IP.Interface.1.Interface.3.IPv4Address.",
            "Device.Reboot()",
        ).forEach {
            val resolved = Path(it)
            assertTrue(resolved.isResolved)
        }

        val expression = Path("Device.IP.Interface.[Name==\"eth0\"].")
        assertFalse(expression.isResolved)

        val wildcard = Path("Device.IP.Interface.*.")
        assertFalse(wildcard.isResolved)
    }

    @Test
    fun `fail to resolve unresolvable paths`() {
        listOf(
            "Device.IP.Interface.*.",
            "Device.IP.Interface.[Name==\"eth0\"]."
        ).forEach {
            assertFailsWith<IllegalArgumentException> {
                ResolvedPath(it)
            }
        }
    }

    @Test
    fun `factory method does not return supported data model paths`() {
        assertFailsWith<IllegalArgumentException> {
            Path("Device.WiFi.SSID.{i}.")
        }
    }

    @Test
    fun `isInstanceOf returns correct results`() {
        val parent = WiFi + "AccessPoint."
        assertTrue((parent + "2.").isInstanceOf(parent))

        assertFalse(parent.isInstanceOf(parent))
        assertFalse((WiFi + "1.").isInstanceOf(parent))
        assertFalse((parent + "1.AssociatedDevice.").isInstanceOf(parent))
        assertFalse((parent + "1.AssociatedDevice.1.").isInstanceOf(parent))
    }
}