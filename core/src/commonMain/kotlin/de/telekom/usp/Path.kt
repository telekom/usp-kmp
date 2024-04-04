package de.telekom.usp

import de.telekom.usp.internal.PathImpl
import de.telekom.usp.internal.PathParser

interface Path {

    /**
     * The list of elements this path consists off.
     */
    val elements: List<PathElement>

    /**
     * The number of path elements of this.
     */
    val size: Int

    /**
     * Returns `true` when this path does not contain any search expressions, `false` otherwise
     */
    val isResolved: Boolean

    val isTerminal: Boolean
        get() = last().isTerminal

    val isCommand: Boolean
        get() = last() is PathElement.Command

    val isEvent: Boolean
        get() = last() is PathElement.Event

    fun asResolvedPath(): ResolvedPath

    operator fun plus(path: String): Path {
        val child = Path(path)

        require(!isTerminal) { "Path '$this' is terminal, cannot append more to it" }
        require(child.first() != Device.first()) { "Can only append relative paths: '$path'" }
        return PathImpl(elements = elements + child.elements)
    }

    fun first(): PathElement = elements.first()

    fun last(): PathElement = elements.last()

    @Suppress("UNCHECKED_CAST")
    fun <T : PathElement> lastAs(): T = elements.last() as T

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
