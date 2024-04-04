package de.telekom.usp

import de.telekom.usp.internal.ResolvedPathImpl

/**
 * Interface identifying a path which is resolved, i.e. contains no search expression.
 */
interface ResolvedPath : Path {

    override operator fun plus(path: String): ResolvedPath {
        val child = Path(path)

        require(!isTerminal) { "Path '$this' is terminal, cannot append more to it" }
        require(child.first() != Device.first()) { "Can only append relative paths: '$path'" }
        require(child.last() !is PathElement.Expression) { "Cannot append a search expression to a resolved path: '$path'" }
        return ResolvedPathImpl(elements = elements + child.elements)
    }
}

/**
 * Factory function to create a resolved path instance from a string.
 *
 * @throws IllegalArgumentException when the `text` does not represent a resolved path
 */
fun ResolvedPath(text: String): ResolvedPath {
    return Path(text).asResolvedPath()
}
