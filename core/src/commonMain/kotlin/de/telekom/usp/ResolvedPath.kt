package de.telekom.usp

/**
 * Marker interface identifying a path which is resolved, i.e. contains no search expression.
 */
interface ResolvedPath : Path

/**
 * Factory function to create a resolved path instance from a string.
 *
 * @throws IllegalArgumentException when the `text` does not represent a resolved path
 */
fun ResolvedPath(text: String): ResolvedPath {
    return Path(text).asResolvedPath()
}
