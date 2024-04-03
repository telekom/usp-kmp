package de.telekom.usp


internal open class PathImpl(final override val elements: List<PathElement>) : Path {

    constructor(vararg paths: PathElement) : this(paths.toList())

    init {
        require(elements.isNotEmpty()) { "Empty Path not allowed" }
    }

    override val size = elements.size

    override val isResolved: Boolean
        get() = elements.isResolved()

    override fun asResolvedPath(): ResolvedPath {
        if (isResolved) {
            return ResolvedPathImpl(elements)
        } else {
            throw IllegalArgumentException("Path not resolved: $this")
        }
    }

    override fun toString(): String {
        return elements.joinToString(separator = "")
    }
}


