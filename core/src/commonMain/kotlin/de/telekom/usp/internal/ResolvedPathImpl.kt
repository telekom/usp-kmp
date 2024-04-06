package de.telekom.usp.internal

import de.telekom.usp.Device
import de.telekom.usp.Path
import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath

internal class ResolvedPathImpl(elements: List<PathElement>) : PathImpl(elements), ResolvedPath {

    internal constructor(vararg paths: PathElement) : this(paths.toList())

    override val isResolved = true

    override fun asResolvedPath() = this

    override operator fun plus(path: String): ResolvedPath {
        val child = Path(path)

        require(!isTerminal) { "Path '$this' is terminal, cannot append more to it" }
        require(child.first() != Device.first()) { "Can only append relative paths: '$path'" }
        require(child.last() !is PathElement.Expression) { "Cannot append a search expression to a resolved path: '$path'" }
        return ResolvedPathImpl(elements = elements + child.elements)
    }

    override fun subPath(fromIndex: Int, toIndex: Int): ResolvedPath =
        ResolvedPathImpl(elements.subList(fromIndex, toIndex))
}