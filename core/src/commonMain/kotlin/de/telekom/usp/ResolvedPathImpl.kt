package de.telekom.usp

internal class ResolvedPathImpl(elements: List<PathElement>) : PathImpl(elements), ResolvedPath {

    override val isResolved = true

    override fun asResolvedPath() = this
}