package de.telekom.usp.internal

import de.telekom.usp.Device
import de.telekom.usp.Path
import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath
import de.telekom.usp.first
import de.telekom.usp.isTerminal

internal class ResolvedPathImpl(elements: List<PathElement>) : PathImpl(elements), ResolvedPath {

    internal constructor(vararg paths: PathElement) : this(paths.toList())

    override val isResolved = true

    override fun asResolvedPath() = this

    override fun add(resolvedPath: String): ResolvedPath {
        val child = Path(resolvedPath)

        require(!isTerminal) { "Path '$this' is terminal, cannot append more to it" }
        require(child.first() != Device.first()) { "Can only append relative paths: '$resolvedPath'" }
        require(child.isResolved) { "'$resolvedPath' is not resolved and cannot be appended to a resolved path: '$this'" }

        return ResolvedPathImpl(elements = elements + child.elements)
    }

    override fun subPath(fromIndex: Int, toIndex: Int): ResolvedPath =
        ResolvedPathImpl(elements.subList(fromIndex, toIndex))

    override fun dropLast(n: Int): ResolvedPath = ResolvedPathImpl(elements.dropLast(n))
}