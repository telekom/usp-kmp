package de.telekom.usp.datamodel

import de.telekom.usp.Device
import de.telekom.usp.datamodel.ExpressionComponent.Companion.matches
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpressionComponentTest {

    @Test
    fun `equals expression matches`() {
        listOf(
            "abc" to Value.Text("abc"),
            "" to Value.Text(""),
            "0" to Value.Number(0),
            "-010" to Value.Number(-10),
            "true" to Value.Boolean.TRUE,
            "1" to Value.Boolean.TRUE,
            "false" to Value.Boolean.FALSE,
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.EQUALS, it.second)
            assertTrue(it.first matches expr)
        }
    }

    @Test
    fun `not equals expression matches`() {
        listOf(
            "abc" to Value.Text("abcdef"),
            "" to Value.Text("abc"),
            "abc" to Value.Text(""),
            "0" to Value.Number(1),
            "-010" to Value.Number(10),
            "true" to Value.Boolean.FALSE,
            "false" to Value.Boolean.TRUE,
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.NOT_EQUALS, it.second)
            assertTrue(it.first matches expr)
        }
    }

    @Test
    fun `contains expression matches`() {
        listOf(
            "A" to Value.Text("A"),
            "NAT44,A+PPortRangeRouter,IPv6Firewall" to Value.Text("A+PPortRangeRouter"),
            "1, 2, 3 , 4" to Value.Text("3"),
            "0,1,2, 3 " to Value.Number(3),
            "true" to Value.Boolean.TRUE,
            "false,true" to Value.Boolean.FALSE,
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.CONTAINS, it.second)
            assertTrue(it.first matches expr)
        }
    }

    @Test
    fun `less than expression matches`() {
        listOf(
            "10" to Value.Number(11),
            "-10" to Value.Number(5),
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.LESS_THAN, it.second)
            assertTrue(it.first matches expr)
        }
    }

    @Test
    fun `less than or equals expression matches`() {
        listOf(
            "10" to Value.Number(11),
            "-10" to Value.Number(-10),
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.LESS_THAN_OR_EQUAL, it.second)
            assertTrue(it.first matches expr)
        }
    }

    @Test
    fun `greater than expression matches`() {
        listOf(
            "10" to Value.Number(9),
            "-10" to Value.Number(-1_000_000),
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.GREATER_THAN, it.second)
            assertTrue(it.first matches expr)
        }
    }

    @Test
    fun `greater than or equals expression matches`() {
        listOf(
            "10000000" to Value.Number(0),
            "-10" to Value.Number(-10),
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.GREATER_THAN_OR_EQUAL, it.second)
            assertTrue(it.first matches expr)
        }
    }

    // --- No match tests --------------------------------------------------------------------------

    @Test
    fun `equals expression does not match`() {
        listOf(
            "abc" to Value.Text("ABC"),
            "" to Value.Text("_"),
            "0" to Value.Number(1),
            "-010" to Value.Number(10),
            "true" to Value.Boolean.FALSE,
            "1" to Value.Boolean.FALSE,
            "false" to Value.Boolean.TRUE,
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.EQUALS, it.second)
            assertFalse(it.first matches expr)
        }
    }

    @Test
    fun `not equals expression does not match`() {
        listOf(
            "abc" to Value.Text("abc"),
            "0" to Value.Number(0),
            "-010" to Value.Number(-10),
            "true" to Value.Boolean.TRUE,
            "false" to Value.Boolean.FALSE,
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.NOT_EQUALS, it.second)
            assertFalse(it.first matches expr)
        }
    }

    @Test
    fun `contains expression does not match`() {
        listOf(
            "" to Value.Text("B"),
            "NAT44,IPv4Firewall" to Value.Text("44"),
            "NAT44,IPv4Firewall" to Value.Text("4"),
            "A, B ,C" to Value.Text("D"),
            "1, 2, 3 , 4" to Value.Text("5"),
            "0,1,2, 3 " to Value.Number(100),
            "true" to Value.Boolean.FALSE,
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.CONTAINS, it.second)
            assertFalse(it.first matches expr)
        }
    }

    @Test
    fun `less than expression does not match`() {
        listOf(
            "10" to Value.Number(9),
            "-10" to Value.Number(-15),
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.LESS_THAN, it.second)
            assertFalse(it.first matches expr)
        }
    }

    @Test
    fun `less than or equals expression does not match`() {
        listOf(
            "10" to Value.Number(9),
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.LESS_THAN_OR_EQUAL, it.second)
            assertFalse(it.first matches expr)
        }
    }

    @Test
    fun `greater than expression does not match`() {
        listOf(
            "10" to Value.Number(19),
            "-10" to Value.Number(1_000_000),
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.GREATER_THAN, it.second)
            assertFalse(it.first matches expr)
        }
    }

    @Test
    fun `greater than or equals expression does not match`() {
        listOf(
            "1" to Value.Number(0),
        ).forEach {
            val expr = ExpressionComponent(Device, Operator.GREATER_THAN_OR_EQUAL, it.second)
            assertTrue(it.first matches expr)
        }
    }
}