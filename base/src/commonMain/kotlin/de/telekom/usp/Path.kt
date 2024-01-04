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

    return PathParser(text).parse()
}

/**
 * Determines whether the specified text is a syntactically valid path. This does not check if the
 * path actually exists on a device. For example "Device" (without trailing dot) is not correct,
 * but we treat it as valid here.
 */
fun isValidPath(text: String): Boolean = runCatching { Path(text) }.isSuccess

inline fun List<Path>.toStrings(): List<String> = this.map { it.toString() }


