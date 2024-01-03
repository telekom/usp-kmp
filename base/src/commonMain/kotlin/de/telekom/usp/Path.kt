package de.telekom.usp


data class Path(val elements: List<PathElement>) {

    constructor(vararg paths: PathElement) : this(paths.toList())

    init {
        require(elements.isNotEmpty()) { "Empty Path not allowed" }
    }

    val size = elements.size

    operator fun plus(path: String): Path {
        val child = Path(path)

        require(!isTerminal()) { "Path '$this' is terminal, cannot append more to it" }
        require(child.first() != Device.first()) { "Cannot append a root path: '$path'" }
        return copy(elements = elements + child.elements)
    }

    fun first(): PathElement = elements.first()

    fun last(): PathElement = elements.last()

    @Suppress("UNCHECKED_CAST")
    fun <T : PathElement> lastAs(): T = elements.last() as T

    fun subPath(fromIndex: Int, toIndex: Int): Path {
        return Path(elements.subList(fromIndex, toIndex))
    }

    /**
     * When `true` this path cannot be extended, i.e. its last element is either of type parameter,
     * command or event.
     */
    fun isTerminal(): Boolean = last().isTerminal

    /**
     * Determines whether the first elements of this path match exactly the specified path
     */
    fun startsWith(path: Path): Boolean {
        if (path.size > this.size) {
            return false
        }
        return this.elements.subList(0, path.size) == path.elements
    }

    /**
     * Determines whether this path starts with 'Device.', that is: if it is an absolute path.
     */
    fun startsWithDevice() = startsWith(Device)

    override fun toString(): String {
        return elements.joinToString(separator = "")
    }
}

/**
 * Factory function to create a path instance from a string.
 */
fun Path(text: String): Path {
    require(text.isNotBlank()) { "Empty Path not allowed" }

    val parts = text.split(dotsOutsideOfBrackets)
    val lastIndex = parts.size - 1
    val elements = parts.mapIndexedNotNull { index, part ->
        if (index == lastIndex) {
            // Skip a trailing '.', as it is already processed in the previous step
            if (part.isNotEmpty()) PathElement.from(part) else null
        } else {
            PathElement.from("$part.")
        }
    }

    return Path(elements)
}

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
        val c = advance()
        when (c) {
            '[' -> parseSearch()
            '(' -> parseCommand()
            '!' -> parseEvent()
            '.' -> parseObject()
        }
    }

    private fun parseObject() {
        when(val name: String = text.substring(start, current - 1)) {
            "*" -> elements.add(WILDCARD)
            "Device" -> elements.add(Device.first())
            else -> {
                val instance = name.toIntOrNull()

                if (instance == null) {
                    val ref = refRegex.find(name)
                    if (ref != null) {
                        val refFollow = ref.value
                        elements.add(PathElement.Object(text.substring(start, current), null, refFollow))
                    } else {
                        elements.add(PathElement.Object(text.substring(start, current)))
                    }
                } else {
                    elements.add(PathElement.Object(text.substring(start, current), instance))
                }
            }
        }
    }

    private fun parseEvent() {
        if (!isAtEnd) {
            val value: String = text.substring(start, current)
            elements.add(PathElement.Event(value))
        } else {
            error("An event must be the last element in a path")
        }
    }

    private fun parseCommand() {
        if (advance() == ')') {
            if (!isAtEnd) {
                error("A command must be the last element in a path")
            }
            val value: String = text.substring(start, current)
            elements.add(PathElement.Command(value))
        } else {
            error("Unmatched '('")
        }
    }

    private fun parseSearch() {
        while (peek() != ']' && !isAtEnd) {
            advance()
        }
        if (isAtEnd) {
            error("Unmatched '['")
        }

        // The closing ].
        advance()

        if (advance() != '.') {
            error("Missing '.' after search expression")
        }
        // Trim the surrounding brackets and dot:
        val value: String = text.substring(start + 1, current - 2)
        elements.add(PathElement.Search(value))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd) return false
        if (text[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char? {
        return if (isAtEnd) {
            null
        } else {
            text[current]
        }
    }

    private fun peekNext(): Char? {
        return if (current + 1 >= text.length) {
            null
        } else {
            text[current + 1]
        }
    }

    private fun advance(): Char = text[current++]

    private fun error(message: String): Nothing {
        throw IllegalArgumentException("$message in '$text' at position $current")
    }


    companion object {

        private val WILDCARD = PathElement.Object("*.", 0, null)

        // Matches for example '#*+' or '#2+' or just '+'
        private val refRegex = Regex("""(#(\*|\d+))?\+$""")
    }
}

/**
 * Determines whether the specified text is a syntactically valid path. This does not check if the
 * path actually exists on a device. For example "Device" (without trailing dot) is not correct,
 * but we treat it as valid here.
 */
fun isValidPath(text: String): Boolean = runCatching { Path(text) }.isSuccess

inline fun List<Path>.toStrings(): List<String> = this.map { it.toString() }

// Dots '.' inside of brackets ('[' and ']') must not be treated as object separators
private val dotsOutsideOfBrackets = Regex("""\.\s*(?![^\[\]]*])""")