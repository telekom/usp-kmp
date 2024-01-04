package de.telekom.usp

internal class PathParser(private val text: String) {
    private var start = 0
    private var current = 0
    private val elements = mutableListOf<PathElement>()

    private val isAtEnd: Boolean
        get() = current >= text.length

    fun parse(): Path {
        while (!isAtEnd) {
            start = current
            parseElement()
        }

        return Path(elements)
    }

    private fun parseElement() {
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
            parameter()
        }
    }

    private fun objectPath() {
        when (val base: String = text.substring(start, current - 1)) {
            "*" -> add(WILDCARD)
            "Device" -> add(Device.first())
            else -> {
                val instance = base.toIntOrNull()
                val name = text.substring(start, current)
                if (instance == null) {
                    val ref = refRegex.find(base)
                    if (ref != null) {
                        add(PathElement.Object(name, null, ref.value))
                    } else {
                        add(PathElement.Object(name))
                    }
                } else {
                    add(PathElement.Object(name, instance))
                }
            }
        }
    }

    private fun event() {
        if (isAtEnd) {
            val base = text.substring(start, current - 1)
            if (isValidName(base)) {
                add(PathElement.Event(text.substring(start, current)))
            } else {
                error("Illegal name '$base'")
            }
        } else {
            error("An event must be the last element in a path")
        }
    }

    private fun command() {
        if (advance() == ')') {
            if (!isAtEnd) {
                error("A command must be the last element in a path")
            }
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

    private fun expression() {
        while (peek() != ']' && !isAtEnd) {
            advance()
        }
        if (isAtEnd) {
            error("Unmatched '['")
        }

        // The closing ].
        advance()

        if (advance() != '.') {
            error("Missing '.' after expression")
        }

        // Good enough for now, later we might need to parse the search string separately...
        val value: String = text.substring(start, current)
        add(PathElement.Expression(value))
    }

    private fun parameter() {
        if (isAtEnd) {
            val name = text.substring(start, current)
            if (isValidName(name)) {
                add(PathElement.Parameter(name))
            } else {
                error("Illegal name '$name'")
            }
        } else {
            error("A parameter must be the last element in a path")
        }
    }

    private fun isValidName(name: String) = validName.matches(name)

    private fun add(element: PathElement) {
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

        private val WILDCARD = PathElement.Object("*.", 0, null)

        // Matches for example '#*+' or '#2+' or just '+'
        private val refRegex = Regex("""(#(\*|\d+))?\+$""")

        private val validName = Regex("""[A-Za-z_][A-Za-z_0-9]*""")
    }
}