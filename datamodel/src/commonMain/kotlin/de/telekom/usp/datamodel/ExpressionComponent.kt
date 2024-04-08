package de.telekom.usp.datamodel

import co.touchlab.kermit.Logger
import de.telekom.usp.Path

data class ExpressionComponent(
    val relpath: Path,
    private val operator: Operator,
    private val value: Value
) {

    private fun matches(left: String): Boolean {
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

        infix fun String.matches(expression: ExpressionComponent) = expression.matches(this)
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
    }

    data class Number(val number: Long) : Value() {

        override fun toString(): String {
            return number.toString()
        }
    }

    class Boolean private constructor(val bool: kotlin.Boolean) : Value() {

        override fun equals(other: Any?): kotlin.Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return if (bool) 1231 else 1237
        }

        override fun toString(): String {
            return if (bool) "true" else "false"
        }

        companion object {

            val TRUE = Boolean(true)

            val FALSE = Boolean(false)
        }
    }

    companion object {

        internal fun from(text: String): Value {
            // USP specification chap. 2.5.4: "Literal values are conceptually converted to a
            // suitable internal representation before comparison. For example, int values 123, +123
            // and 0123 all represent the same value, and so do boolean values 1 and true."

            return if (text == "true" || text == "1") {
                Boolean.TRUE
            } else if (text == "false") {
                Boolean.FALSE
            } else {
                val num = text.toLongOrNull()
                if (num != null) Number(num) else Text(text)
            }
        }
    }
}


private val EvalEquals: (Value, Value) -> Boolean = { left, right -> left == right }

private val EvalNotEquals: (Value, Value) -> Boolean = { left, right -> left != right }

private val EvalContains: (Value, Value) -> Boolean = { left, right ->
    // Do NOT use `left.toString().contains(right.toString())` as this may return false
    // positives, for example: "NAT44".contains("44")  == true" which is not meant here!
    left.toString().split(",").map { it.trim() }.contains(right.toString())
}

private val EvalLess: (Value, Value) -> Boolean = { left, right ->
    val leftInt = if (left === Value.Boolean.TRUE) 1 else (left as Value.Number).number
    val rightInt = if (right === Value.Boolean.TRUE) 1 else (right as Value.Number).number
    leftInt < rightInt
}

private val EvalGreater: (Value, Value) -> Boolean = { left, right ->
    val leftInt = if (left === Value.Boolean.TRUE) 1 else (left as Value.Number).number
    val rightInt = if (right === Value.Boolean.TRUE) 1 else (right as Value.Number).number
    leftInt > rightInt
}

private val EvalLessEqual: (Value, Value) -> Boolean = { left, right ->
    val leftInt = if (left === Value.Boolean.TRUE) 1 else (left as Value.Number).number
    val rightInt = if (right === Value.Boolean.TRUE) 1 else (right as Value.Number).number
    leftInt <= rightInt
}

private val EvalGreaterEqual: (Value, Value) -> Boolean = { left, right ->
    val leftInt = if (left === Value.Boolean.TRUE) 1 else (left as Value.Number).number
    val rightInt = if (right === Value.Boolean.TRUE) 1 else (right as Value.Number).number
    leftInt >= rightInt
}
