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

    return Path(parts.mapIndexedNotNull { index, part ->
        if (index == lastIndex) {
            // Skip a trailing '.', as it is already processed in the previous step
            if (part.isNotEmpty()) PathElement.from(part) else null
        } else {
            PathElement.from("$part.")
        }
    })
}

// Dots '.' inside of brackets ('[' and ']') must not be treated as object separators
private val dotsOutsideOfBrackets = Regex("""\.\s*(?![^\[\]]*])""")