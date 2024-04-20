package de.telekom.usp.util

fun String.camelCaseToBlanks(): String {
    return windowed(2, 1, true) { chars ->
        if (chars.length > 1) {
            if (chars[0].isLowerCase() && chars[1].isUpperCase()) {
                "${chars[0]} "
            } else {
                chars[0].lowercaseChar()
            }
        } else {
            chars[0].lowercaseChar()
        }
    }.joinToString(separator = "").replaceFirstChar { it.uppercaseChar() }
}
