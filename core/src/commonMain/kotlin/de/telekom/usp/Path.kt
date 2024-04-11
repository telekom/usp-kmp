package de.telekom.usp

import de.telekom.usp.internal.PathParser

/**
 * Represents a USP path.
 *
 * @see ResolvedPath
 * @see SupportedDataModelPath
 */
interface Path {

    /**
     * The list of elements this path consists off.
     */
    val elements: List<PathElement>

    fun asResolvedPath(): ResolvedPath

    operator fun plus(path: String): Path

    /**
     * Returns a path containing the results of applying the given [transform] function
     * to each path element and its index in the original path.
     *
     * @param [transform] function that takes the index of a path element and the path element
     *        itself and returns the result of the transform applied to the path element.
     */
    fun mapIndexed(transform: (index: Int, PathElement) -> PathElement): Path

    /**
     * Returns a view of the portion of this `Path` between the specified [fromIndex] (inclusive)
     * and [toIndex] (exclusive).
     */
    fun subPath(fromIndex: Int, toIndex: Int): Path

    /**
     * Returns a path containing all elements except last n elements.
     */
    fun dropLast(n: Int): Path

    /**
     * The number of path elements of this.
     */
    val size: Int
        get() = elements.size

    /**
     * Returns `true` when this path does not contain any unresolved path elements
     *
     * @see [PathElement.isResolved]
     */
    val isResolved: Boolean
        get() = elements.isResolved()

    val isTerminal: Boolean
        get() = last().isTerminal

    val isParameter: Boolean
        get() = last() is PathElement.Parameter

    val isCommand: Boolean
        get() = last() is PathElement.Command

    val isEvent: Boolean
        get() = last() is PathElement.Event

    operator fun get(index: Int) = elements[index]

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
 * Factory function to create a path instance from a string. Don't use this function to create
 * supported data model paths (i.e. paths which contain "{i}")! See the [SupportedDataModelPath]
 * factory function for this.
 */
fun Path(text: String): Path {
    require(text.isNotBlank()) { "Empty Path not allowed" }

    val path = PathParser(text).parse()
    if (path is SupportedDataModelPath) {
        throw IllegalArgumentException("Use SupportedDataModelPath(String) to create supported data model paths")
    }
    return path
}

/**
 * Determines whether the specified text is a syntactically valid path. This does not check if the
 * path actually exists on a device. For example "Device" (without trailing dot) is not correct,
 * but we treat it as valid here.
 */
fun isValidPath(text: String): Boolean = runCatching { Path(text) }.isSuccess

inline fun List<Path>.toStrings(): List<String> = this.map { it.toString() }
