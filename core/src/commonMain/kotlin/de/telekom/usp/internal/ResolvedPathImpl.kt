package de.telekom.usp.internal

import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath

internal class ResolvedPathImpl(elements: List<PathElement>) : PathImpl(elements), ResolvedPath {

    internal constructor(vararg paths: PathElement) : this(paths.toList())

    override val isResolved = true

    override fun asResolvedPath() = this
}