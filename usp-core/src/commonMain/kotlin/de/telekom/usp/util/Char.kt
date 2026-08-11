/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.util

fun Char.isHexDigit(): Boolean {
    return when (this) {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f' -> true
        else -> false
    }
}

/**
 * See [Documentation of instance-id](https://usp.technology/specification/index.htm#r-arc.5)
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