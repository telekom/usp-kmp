/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp

import de.telekom.usp.internal.PathParser

/**
 * Represents a USP path.
 *
 * @see ResolvedPath
 * @see SupportedDataModelPath
 */
interface Path {

    /**
     * The list of elements this path consists off.
     */
    val elements: List<PathElement>

    /**
     * Returns `true` when this path does not contain any unresolved path elements. Note that this
     * does not imply that you can safely cast this to [ResolvedPath], use [asResolvedPath] instead.
     *
     * @see [PathElement.isResolved]
     */
    val isResolved: Boolean

    fun asResolvedPath(): ResolvedPath

    operator fun plus(path: String): Path

    /**
     * Replaces the specified [PathElement] at the specified position and returns a new instances
     * of [Path] with the replaced path elements.
     */
    fun replace(index: Int, replacement: PathElement): Path

    /**
     * Returns a path containing the results of applying the given [transform] function
     * to each path element and its index in the original path.
     *
     * @param [transform] function that takes the index of a path element and the path element
     *        itself and returns the result of the transform applied to the path element.
     */
    fun mapIndexed(transform: (index: Int, PathElement) -> PathElement): Path

    /**
     * Returns a view of the portion of this `Path` between the specified [fromIndex] (inclusive)
     * and [toIndex] (exclusive).
     */
    fun subPath(fromIndex: Int, toIndex: Int): Path

    /**
     * Returns a path containing all elements except last n elements.
     */
    fun dropLast(n: Int = 1): Path

    /**
     * Must return the path value of this as a string.
     */
    override fun toString(): String
}

// According to https://kotlinlang.org/docs/api-guidelines-minimizing-mental-complexity.html#use-extension-functions-and-properties
// we define all none strict interface methods of Path as extension functions (rather than default functions):

/**
 * The number of path elements of this.
 */
val Path.size: Int
    get() = elements.size

/**
 * Returns `true` if no path element can be appended to this path, i.e. when it is a parameter,
 * a command or an event.
 */
val Path.isTerminal: Boolean
    get() = last().isTerminal

/**
 * Returns `true` when this path represents is a parameter
 */
val Path.isParameter: Boolean
    get() = last() is PathElement.Parameter

/**
 * Returns `true` when this path represents is a command
 */
val Path.isCommand: Boolean
    get() = last() is PathElement.Command

/**
 * Returns `true` when this path represents is an event
 */
val Path.isEvent: Boolean
    get() = last() is PathElement.Event

/**
 * Returns `true` when the last path element is an instance number, i.e.
 * `Device.WiFi.AccessPoint.2.`. Returns `false` for wildcard instances and all other paths.
 */
val Path.isInstance: Boolean
    get() = last().let { it is PathElement.Object && it.instance != null && it.instance > 0 }

operator fun Path.get(index: Int) = elements[index]

fun Path.first(): PathElement = elements.first()

fun Path.last(): PathElement = elements.last()

@Suppress("UNCHECKED_CAST")
fun <T : PathElement> Path.lastAs(): T = elements.last() as T

/**
 * Determines whether the first elements of this path match exactly the specified path
 */
fun Path.startsWith(path: Path): Boolean {
    if (path.size > this.size) {
        return false
    }
    return this.elements.subList(0, path.size) == path.elements
}

/**
 * Determines whether the last elements of this path match exactly the specified path
 */
fun Path.endsWith(path: Path): Boolean {
    if (path.size > this.size) {
        return false
    }
    return this.elements.subList(size - path.size, size) == path.elements
}

/**
 * Determines whether this path is an instantiated object of the specified path. For example,
 * if this is `Device.WiFi.AccessPoint.2.` then this method returns `true`, if `path` is
 * `Device.WiFi.AccessPoint.` and `false` for all other paths. Specifically, it returns `false`
 * when `path` is for example `Device.WiFi.AccessPoint.2.Security.`.
 */
fun Path.isInstanceOf(path: Path): Boolean {
    return path.size == size - 1 && isInstance && startsWith(path)
}

/**
 * Determines whether this path starts with 'Device.', that is: if it is an absolute path.
 */
fun Path.startsWithDevice() = startsWith(Device)

/**
 * Returns a string representation of this path as a reference, i.e. without the trailing dot at the end.
 *
 * @throws IllegalStateException when this path is a command, a parameter or an event, as they cannot be converted
 *         into a reference
 */
fun Path.toStringAsReference(): String {
    return when (last()) {
        is PathElement.Command -> {
            throw IllegalStateException("Cannot convert a command path ($this) into a reference")
        }

        is PathElement.Event -> {
            throw IllegalStateException("Cannot convert an event path ($this) into a reference")
        }

        is PathElement.Parameter -> {
            throw IllegalStateException("Cannot convert a parameter path ($this) into a reference")
        }

        else -> {
            buildString {
                val limit = elements.size - 1
                elements.forEachIndexed { index, element ->
                    if (index < limit) {
                        append(element)
                    } else {
                        append(element.text.removeSuffix("."))
                    }
                }
            }
        }
    }
}

/**
 * Factory function to create a path instance from a string. Don't use this function to create
 * supported data model paths (i.e. paths which contain "{i}")! See the [SupportedDataModelPath]
 * factory function for this.
 *
 * @param isReference when `true` parses the text as a path reference. Path references do not contain dots at the end.
 *        Hence, the resulting path will contain an object element as the last path element, while a path which is not
 *        a reference and ends without a dot, will contain a parameter as the last path element!
 */
fun Path(text: String, isReference: Boolean = false): Path {
    require(text.isNotBlank()) { "Empty Path not allowed" }

    val path = PathParser(text).parse(asReferencePath = isReference)
    if (path is SupportedDataModelPath) {
        throw IllegalArgumentException("Use SupportedDataModelPath(String) to create supported data model paths")
    }
    return path
}

fun String.toPath() = Path(this)

/**
 * See the `Path(String, Boolean)` function.
 */
fun String.toPathFromReference() = Path(this, true)

fun String.toPathOrNull(): Path? {
    return try {
        Path(this)
    } catch (ex: Exception) {
        null
    }
}

/**
 * Determines whether the specified text is a syntactically valid path. This does not check if the
 * path actually exists on a device. For example "Device" (without trailing dot) is not correct,
 * but we treat it as valid here.
 */
fun isValidPath(text: String): Boolean = runCatching { Path(text) }.isSuccess

inline fun List<Path>.toStrings(): List<String> {
    return if (isEmpty()) emptyList() else map { it.toString() }
}

inline fun List<Path>.asResolvedList(): List<ResolvedPath> {
    return if (isEmpty()) emptyList() else map { it.asResolvedPath() }
}
