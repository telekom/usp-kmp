package de.telekom.usp.internal

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

    override fun toString(): String {
        return elements.joinToString(separator = "")
    }
}


