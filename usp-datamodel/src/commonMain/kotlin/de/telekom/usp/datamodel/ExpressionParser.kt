/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.datamodel

import de.telekom.usp.Path
import de.telekom.usp.PathElement

/**
 * Converts the components of [PathElement.Expression] into instances of [ExpressionComponent].
 */
internal class ExpressionParser(private val expression: PathElement.Expression) {

    /**
     * Parses the expression components of this into a list of [ExpressionComponent] instances.
     *
     * @throws ExpressionParserException when one of the expression components cannot be parsed
     */
    fun parse(): List<ExpressionComponent> {
        return expression.components.mapIndexed { index, component ->
            ExpressionComponentParser(index, component).parse()
        }
    }

    private inner class ExpressionComponentParser(val expressionIndex: Int, val text: String) {

        private var start = 0
        private var current = 0
        private val isAtEnd: Boolean
            get() = current >= text.length

        fun parse(): ExpressionComponent {
            return ExpressionComponent(path(), operator(), value())
        }

        private fun path(): Path {
            while (!isAtEnd) {
                when (advance()) {
                    '=', '!', '>', '<', '~' -> break
                }
            }
            if (current == 0) {
                error("missing path")
            }

            val tmp = text.substring(0, current - 1).trim()
            start = current - 1

            return try {
                Path(tmp)
            } catch (ex: Exception) {
                error("invalid relative path: '$tmp' (${ex.message})")
            }
        }

        private fun operator(): Operator {
            while (!isAtEnd) {
                when (advance()) {
                    '=', ' ' -> continue
                    else -> break
                }
            }
            val tmp = text.substring(start, current - 1)
            if (tmp.isBlank()) {
                error("missing operator")
            }

            start = current - 1

            return Operator.from(tmp.trim()) ?: error("unknown operator '$tmp'")
        }

        private fun value(): Value {
            var tmp = text.substring(start)
            if (tmp.isBlank()) {
                error("missing value")
            }

            tmp = tmp
                .trim(' ', '"')
                .replace("%22", "\"")
                .replace("%25", "%")

            return Value.from(tmp)
        }

        private fun advance(): Char = text[current++]

        private fun error(message: String): Nothing {
            throw ExpressionParserException(
                expression,
                expressionIndex,
                "$message in '$text' at position $current"
            )
        }
    }
}