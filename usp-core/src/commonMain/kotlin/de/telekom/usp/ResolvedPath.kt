/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp

/**
 * Interface identifying a path which is resolved, i.e. contains no search expression.
 */
interface ResolvedPath : Path {

    /**
     * Adds the specified resolved path and returns a new instance of resolved path.
     *
     * @throws IllegalArgumentException when [resolvedPath] string cannot be converted into a
     *         resolved path. In contrary to this method, the plus operator function of [Path]
     *         also accepts non resolved paths. We intentionally do not override the operator
     *         function, to avoid confusions with different return types.
     */
    fun add(resolvedPath: String): ResolvedPath

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