package de.telekom.usp

object Versions {

    const val mostRecent = "1.3"

    private val supported = listOf("1.2", mostRecent)

    fun isSupported(version: String) = supported.contains(version)

    override fun toString(): String {
        return "Supported versions: ${supported.joinToString()}"
    }
}