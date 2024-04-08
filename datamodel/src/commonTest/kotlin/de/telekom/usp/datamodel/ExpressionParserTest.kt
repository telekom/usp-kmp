package de.telekom.usp.datamodel

import de.telekom.usp.Path
import de.telekom.usp.PathElement
import de.telekom.usp.datamodel.Operator.CONTAINS
import de.telekom.usp.datamodel.Operator.EQUALS
import de.telekom.usp.datamodel.Operator.GREATER_THAN
import de.telekom.usp.datamodel.Operator.NOT_EQUALS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExpressionParserTest {

    @Test
    fun `parse expressions correctly`() {
        listOf(
            "[Name==\"eth0\"]." to textExp("Name", EQUALS, "eth0"),
            "[Status==\"Enabled\"]." to textExp("Status", EQUALS, "Enabled"),
            "[Enabled!=\"true\"]." to boolExp("Enabled", NOT_EQUALS, true),
            "[Enabled==\"false\"]." to boolExp("Enabled", EQUALS, false),
            "[Stats.ErrorsSent>0]." to numExp("Stats.ErrorsSent", GREATER_THAN, 0L),
            "[Capabilities~=\"IPv6Firewall\"]." to textExp(
                "Capabilities",
                CONTAINS,
                "IPv6Firewall"
            ),
            "[Name==\"%22test-%2525%22\"]." to textExp("Name", EQUALS, "\"test-%25\""),
        ).forEach {
            val exp = Path(it.first).first() as PathElement.Expression
            val actual = ExpressionParser(exp).parse().first()
            assertEquals(actual, it.second)
        }
    }

    @Test
    fun `parse expressions with blanks correctly`() {
        val expected = textExp("Status", EQUALS, "Enabled")

        listOf(
            "[Status == \"Enabled\"].",
            "[ Status==\"Enabled\" ].",
            "[  Status  ==  \"Enabled\"  ].",
        ).forEach {
            val exp = Path(it).first() as PathElement.Expression
            val actual = ExpressionParser(exp).parse().first()
            assertEquals(actual, expected)
        }
    }

    @Test
    fun `throws exception for illegal expressions`() {
        listOf(
            "[Name==\"eth0\"&& ].",
            "[Name].",
            "[Name=\"eth0\"].",
            "[Name==  ].",
            "[ ==\"eth0\"].",
        ).forEach {
            assertFailsWith<ExpressionParserException> {
                ExpressionParser(Path(it).first() as PathElement.Expression).parse()
            }
        }
    }

    private fun textExp(path: String, operator: Operator, text: String) =
        ExpressionComponent(Path(path), operator, Value.Text(text))

    private fun numExp(path: String, operator: Operator, num: Long) =
        ExpressionComponent(Path(path), operator, Value.Number(num))

    private fun boolExp(path: String, operator: Operator, bool: Boolean) =
        ExpressionComponent(
            Path(path),
            operator,
            if (bool) Value.Boolean.TRUE else Value.Boolean.FALSE
        )
}