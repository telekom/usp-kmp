package de.telekom.usp.internal

import de.telekom.usp.Device
import de.telekom.usp.Path
import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath
import de.telekom.usp.first
import de.telekom.usp.isResolved
import de.telekom.usp.isTerminal


internal open class PathImpl(final override val elements: List<PathElement>) : Path {

    constructor(vararg paths: PathElement) : this(paths.toList())

    init {
        require(elements.isNotEmpty()) { "Empty Path not allowed" }
    }

    override val isResolved: Boolean
        get() = elements.isResolved()

    override fun asResolvedPath(): ResolvedPath {
        if (isResolved) {
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

    override fun replace(index: Int, replacement: PathElement): Path {
        return PathImpl(elements.toMutableList().also { newList ->
            newList[index] = replacement
        })
    }

    override fun mapIndexed(transform: (index: Int, PathElement) -> PathElement) =
        PathImpl(elements.mapIndexed(transform))


    override fun subPath(fromIndex: Int, toIndex: Int): Path =
        PathImpl(elements.subList(fromIndex, toIndex))

    override fun dropLast(n: Int): Path = PathImpl(elements.dropLast(n))

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PathImpl

        return elements == other.elements
    }

    override fun toString(): String {
        return elements.joinToString(separator = "")
    }
}


