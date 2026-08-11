/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.internal

import de.telekom.usp.*
import kotlin.test.*


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
    fun `parses reference paths without error`() {
        val expected = Path("Device.WiFi.SSID.1.")
        assertEquals(expected, Path("Device.WiFi.SSID.1", true))
        assertEquals(expected, Path("Device.WiFi.SSID.1.", true))
    }

    @Test
    fun `toStringAsReference removes the trailing dot`() {
        val path = Path("Device.WiFi.SSID.1.")
        assertEquals("Device.WiFi.SSID.1", path.toStringAsReference())

        assertFailsWith<IllegalStateException> { Path("Device.Reboot()").toStringAsReference() }
        assertFailsWith<IllegalStateException> { Path("Device.Event!").toStringAsReference() }
        assertFailsWith<IllegalStateException> { Path("Device.Parameter").toStringAsReference() }
    }

    @Test
    fun `equals matches paths correctly`() {
        assertEquals(Path("Device.IP.Interface.*."), Path("Device.IP.Interface.*."))
        assertEquals(Path("Parameter"), Path("Parameter"))
        assertEquals(Path("Device.Reboot()"), Path("Device.Reboot()"))
        assertEquals(IP + "Interface.", IP + "Interface.")

        assertNotEquals(Path("Parameter."), Path("Parameter"))

        // Special case: ResolvedPath equals Path which is resolved when path elements match
        val resolved = Path("Device.IP.Interface.1.").asResolvedPath()
        assertEquals(Path("Device.IP.Interface.1."), resolved)
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
    fun `returns a resolved path instance when path is resolved`() {
        val resolved = Path("Device.WiFi.SSID.1.")
        assertIs<ResolvedPath>(resolved.asResolvedPath())

        val notResolved = Path("Device.IP.Interface.*.Type")
        assertFailsWith<IllegalArgumentException> {
            notResolved.asResolvedPath()
        }
    }

    @Test
    fun `replace replaces the proper path elements`() {
        val path = Path("Device.WiFi.SSID.1.")
        val replaced = path.replace(1, Path("IP.").elements[0])

        assertEquals(Path("Device.IP.SSID.1."), replaced)
    }

    @Test
    fun `mapIndexed creates a mapped path`() {
        val path = Path("Device.WiFi.SSID.1.")
        val mapped = path.mapIndexed { index, element ->
            if (index == 2) Path("IP.").elements[0]
            else if (index == 3) Path("4.").elements[0]
            else element
        }

        assertEquals(Path("Device.WiFi.IP.4."), mapped)
    }

    @Test
    fun `subPath returns a view of the original path`() {
        val path = Path("Device.WiFi.SSID.1.")
        val subPath = path.subPath(1, 3)

        assertEquals(Path("WiFi.SSID."), subPath)
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

    @Test
    fun `dropLast drops the correct number of path elements`() {
        val path = Path("Device.IP.Interface.1.Interface.3.IPv4Address.")
        assertEquals(Path("Device.IP.Interface.1.Interface.3.IPv4Address."), path.dropLast(0))
        assertEquals(Path("Device.IP.Interface.1.Interface.3."), path.dropLast())
        assertEquals(Path("Device.IP.Interface.1.Interface."), path.dropLast(2))
        assertEquals(Path("Device.IP.Interface.1."), path.dropLast(3))
    }

    @Test
    fun `toString returns the path value`() {
        listOf(
            "Device.IP.Interface.",
            "Device.IP.Interface.Status",
            "Device.IP.Interface.1.",
            "Device.IP.Interface.1.Interface.3.IPv4Address.",
            "Device.Reboot()",
        ).forEach {
            assertEquals(it, Path(it).toString())
        }
    }
}