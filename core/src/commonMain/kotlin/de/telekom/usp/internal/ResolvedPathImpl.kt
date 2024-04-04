package de.telekom.usp.internal

import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath

internal class ResolvedPathImpl(elements: List<PathElement>) : PathImpl(elements), ResolvedPath {

    override val isResolved = true

    override fun asResolvedPath() = this
}