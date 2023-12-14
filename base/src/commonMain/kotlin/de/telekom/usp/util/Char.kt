package de.telekom.usp.util

fun Char.isHexDigit(): Boolean {
    return when (this) {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f' -> true
        else -> false
    }
}

/**
 * See [Documentation of instance-id](https://github.com/BroadbandForum/usp/blob/master/specification/architecture/index.md#use-of-instance-id)
 */
internal fun Char.isUnreserved(): Boolean {
    return when (this) {
        in '0' .. '9' -> true
        in 'A'..'Z' -> true
        in 'a'..'z' -> true
        '-', '.', '_' -> true
        else -> false
    }
}