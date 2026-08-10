/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.internal

import de.telekom.usp.*

internal class PathParser(private val text: String) {
    private var start = 0
    private var current = 0
    private var containsPlaceholder = false
    private val elements = mutableListOf<PathElement>()

    private val isAtEnd: Boolean
        get() = current >= text.length

    fun parse(asSupportedDataModelPath: Boolean = false, asReferencePath: Boolean = false): Path {
        current = 0
        elements.clear()

        while (!isAtEnd) {
            start = current
            parseElement(asReferencePath)
        }

        return if (containsPlaceholder || asSupportedDataModelPath) {
            SupportedDataModelPathImpl(elements)
        } else if (elements.isResolved()) {
            ResolvedPathImpl(elements)
        } else {
            PathImpl(elements)
        }
    }

    private fun parseElement(asReferencePath: Boolean) {
        while (!isAtEnd) {
            when (advance()) {
                '[' -> {
                    expression()
                    return
                }

                '(' -> {
                    command()
                    return
                }

                '!' -> {
                    event()
                    return
                }

                '.' -> {
                    objectPath()
                    return
                }
            }
        }

        if (start < current) {
            if (asReferencePath) {
                objectPath()
            } else {
                parameter()
            }
        }
    }

    private fun objectPath() {
        when (val base: String = text.substring(start, current - 1)) {
            "*" -> add(Wildcard)
            "Device" -> add(Device.first())
            "{i}" -> {
                containsPlaceholder = true
                add(PathElement.Placeholder)
            }

            else -> {
                val instance = base.toIntOrNull()
                val name = with(text.substring(start, current)) {
                    // When parsing a reference path, the last dot will be omitted, add it here:
                    if (last() == '.') {
                        this
                    } else {
                        "$this."
                    }
                }

                if (instance == null) {
                    add(PathElement.Object(name, null, ReferenceFollowing.from(base)))
                } else {
                    add(PathElement.Object(name, instance))
                }
            }
        }
    }

    private fun expression() {
        while (peek() != ']' && !isAtEnd) {
            advance()
        }
        if (isAtEnd) {
            error("Unmatched '['")
        }

        // Consume the closing ].
        advance()

        if (advance() != '.') {
            error("Missing '.' after expression")
        }

        // Good enough for now, later we might need to parse the search expression separately...
        add(PathElement.Expression(text.substring(start, current)))
    }

    private fun event() {
        val base = text.substring(start, current - 1)
        if (isValidName(base)) {
            add(PathElement.Event(text.substring(start, current)))
        } else {
            error("Illegal name '$base'")
        }
    }

    private fun command() {
        if (advance() == ')') {
            val base = text.substring(start, current - 2)
            if (isValidName(base)) {
                add(PathElement.Command(text.substring(start, current)))
            } else {
                error("Illegal name '$base'")
            }
        } else {
            error("Unmatched '('")
        }
    }

    private fun parameter() {
        val name = text.substring(start, current)
        if (isValidName(name)) {
            add(PathElement.Parameter(name))
        } else {
            error("Illegal name '$name'")
        }
    }

    private fun isValidName(name: String) = validName.matches(name)

    private fun add(element: PathElement) {
        if (element.isTerminal && !isAtEnd) {
            error("'$element' must be the last element in a path")
        }
        elements.add(element)
    }

    private fun peek(): Char? {
        return if (isAtEnd) null else text[current]
    }

    private fun advance(): Char = text[current++]

    private fun error(message: String): Nothing {
        throw IllegalArgumentException("$message in '$text' at position $current")
    }


    companion object {

        private val Wildcard = PathElement.Object("*.", 0, null)

        private val validName = Regex("""[A-Za-z_][A-Za-z_0-9]*""")
    }
}