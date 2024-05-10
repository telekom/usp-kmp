package de.telekom.usp

/**
 * Interface identifying a path which is resolved, i.e. contains no search expression.
 */
interface ResolvedPath : Path {

    override operator fun plus(path: String): ResolvedPath

    /**
     * Returns a view of the portion of this `ResolvedPath` between the specified [fromIndex]
     * (inclusive) and [toIndex] (exclusive).
     */
    override fun subPath(fromIndex: Int, toIndex: Int): ResolvedPath


    /**
     * Returns a resolved path containing all elements except last n elements.
     */
    override fun dropLast(n: Int): ResolvedPath
}

/**
 * Factory function to create a resolved path instance from a string.
 *
 * @throws IllegalArgumentException when the `text` does not represent a resolved path
 */
fun ResolvedPath(text: String): ResolvedPath {
    return Path(text).asResolvedPath()
}

fun String.toResolvedPath() = ResolvedPath(this)