package de.telekom.usp

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

    fun asResolvedPath(): ResolvedPath
}

/**
 * Factory function to create a path instance from a string.
 */
fun Path(text: String): Path {
    require(text.isNotBlank()) { "Empty Path not allowed" }

    return PathParser(text).parse()
}

operator fun Path.plus(path: String): Path {
    val child = Path(path)

    require(!isTerminal) { "Path '$this' is terminal, cannot append more to it" }
    require(child.first() != Device.first()) { "Cannot append a root path: '$path'" }
    return PathImpl(elements = elements + child.elements)
}

fun Path.first(): PathElement = elements.first()

fun Path.last(): PathElement = elements.last()

val Path.isTerminal: Boolean
    get() = last().isTerminal

val Path.isCommand: Boolean
    get() = last() is PathElement.Command

val Path.isEvent: Boolean
    get() = last() is PathElement.Event

@Suppress("UNCHECKED_CAST")
fun <T : PathElement> Path.lastAs(): T = elements.last() as T

/**
 * Determines whether the first elements of this path match exactly the specified path
 */
fun Path.startsWith(path: Path): Boolean {
    if (path.size > this.size) {
        return false
    }
    return this.elements.subList(0, path.size) == path.elements
}

/**
 * Determines whether this path starts with 'Device.', that is: if it is an absolute path.
 */
fun Path.startsWithDevice() = startsWith(Device)

