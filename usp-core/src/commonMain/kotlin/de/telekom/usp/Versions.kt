package de.telekom.usp

object Versions {

    const val MOST_RECENT = "1.3"

    private val supported = listOf("1.0", "1.1", "1.2", MOST_RECENT)

    fun isSupported(version: String) = supported.contains(version)

    override fun toString(): String {
        return "Supported versions: ${supported.joinToString()}"
    }
}