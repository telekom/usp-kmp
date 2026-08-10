/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.datamodel

import co.touchlab.kermit.Logger
import de.telekom.usp.Path
import kotlinx.datetime.Instant

data class ExpressionComponent(
    val relpath: Path,
    private val operator: Operator,
    private val value: Value
) {

    private fun matches(left: String?): Boolean {
        if (left == null) {
            return false
        }

        return try {
            val leftValue = Value.from(left)
            val result = operator.evaluate(leftValue, value)

            Logger.d { "Expression result of '$left $operator $value' is: $result" }
            result
        } catch (ex: Exception) {
            Logger.e(throwable = ex) { "Error evaluating '$left $operator $value' (${ex.message})" }
            false
        }
    }

    companion object {

        infix fun String?.matches(expression: ExpressionComponent) = expression.matches(this)
    }
}

enum class Operator(private val op: String, private val evaluator: (Value, Value) -> Boolean) {

    EQUALS("==", EvalEquals),
    NOT_EQUALS("!=", EvalNotEquals),
    CONTAINS("~=", EvalContains),
    LESS_THAN("<", EvalLess),
    GREATER_THAN(">", EvalGreater),
    LESS_THAN_OR_EQUAL("<=", EvalLessEqual),
    GREATER_THAN_OR_EQUAL(">=", EvalGreaterEqual);

    fun evaluate(left: Value, right: Value) = evaluator(left, right)

    override fun toString(): String {
        return op
    }

    companion object {
        fun from(text: String) = entries.firstOrNull { it.op == text }
    }
}

sealed class Value {

    data class Text(val text: String) : Value() {

        override fun toString(): String {
            return text
        }

        companion object {
            val Empty = Text("")
        }
    }

    data class Numeric(val number: Long) : Value() {

        override fun toString(): String {
            return number.toString()
        }

        companion object {
            val Zero = Numeric(0L)

            val One = Numeric(1L)
        }
    }

    companion object {

        internal fun from(text: String): Value {
            // USP specification chap. 2.5.4: "Literal values are conceptually converted to a
            // suitable internal representation before comparison. For example, int values 123, +123
            // and 0123 all represent the same value, and so do boolean values 1 and true."
            //
            // Hence we do not support a data type Boolean, as we cannot distinguish between 1 and
            // true nor 0 and false. And also date/time is converted into integer types:

            return if (text.isEmpty()) {
                Text.Empty
            } else if (text == "1" || text == "true") {
                Numeric.One
            } else if (text == "0" || text == "false") {
                Numeric.Zero
            } else {
                val num = text.toLongOrNull()

                // If text is not an integer, but starts with a digit and has a reasonable length,
                // try to parse is as date/time:
                if (num == null && text[0].isDigit() && text.length > 18) {
                    try {
                        Numeric(Instant.parse(text).toEpochMilliseconds())
                    } catch (ex: IllegalArgumentException) {
                        Text(text)
                    }
                } else if (num != null) {
                    Numeric(num)
                } else {
                    Text(text)
                }
            }
        }
    }
}


private val EvalEquals: (Value, Value) -> Boolean = { left, right -> left == right }

private val EvalNotEquals: (Value, Value) -> Boolean = { left, right -> left != right }

private val EvalContains: (Value, Value) -> Boolean = { left, right ->
    // Do NOT use `left.toString().contains(right.toString())` as this may return false
    // positives, for example: "NAT44,XYZ".contains("44")  == true" which is not meant here!
    // However this still doesn't match "true,false ~= 1" which is acceptable for now.
    left.toString().split(",").map { it.trim() }.contains(right.toString())
}

private val EvalLess: (Value, Value) -> Boolean = { left, right ->
    (left as Value.Numeric).number < (right as Value.Numeric).number
}

private val EvalGreater: (Value, Value) -> Boolean = { left, right ->
    (left as Value.Numeric).number > (right as Value.Numeric).number
}

private val EvalLessEqual: (Value, Value) -> Boolean = { left, right ->
    (left as Value.Numeric).number <= (right as Value.Numeric).number
}

private val EvalGreaterEqual: (Value, Value) -> Boolean = { left, right ->
    (left as Value.Numeric).number >= (right as Value.Numeric).number
}
