package de.telekom.usp.mtp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform