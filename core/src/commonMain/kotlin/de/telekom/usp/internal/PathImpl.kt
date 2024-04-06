package de.telekom.usp.internal

import de.telekom.usp.Device
import de.telekom.usp.Path
import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath
import de.telekom.usp.isResolved


internal open class PathImpl(final override val elements: List<PathElement>) : Path {

    constructor(vararg paths: PathElement) : this(paths.toList())

    init {
        require(elements.isNotEmpty()) { "Empty Path not allowed" }
    }

    override val size = elements.size

    override val isResolved = elements.isResolved()

    override fun asResolvedPath(): ResolvedPath {
        if (isResolved) {
            // Should not happen as the PathParser creates a ResolvedPathImpl from a resolvable path
            return ResolvedPathImpl(elements)
        } else {
            throw IllegalArgumentException("Path not resolved: $this")
        }
    }

    override operator fun plus(path: String): Path {
        val child = Path(path)

        require(!isTerminal) { "Path '$this' is terminal, cannot append more to it" }
        require(child.first() != Device.first()) { "Can only append relative paths: '$path'" }
        return PathImpl(elements = elements + child.elements)
    }

    override fun subPath(fromIndex: Int, toIndex: Int): Path =
        PathImpl(elements.subList(fromIndex, toIndex))

    override fun toString(): String {
        return elements.joinToString(separator = "")
    }
}


