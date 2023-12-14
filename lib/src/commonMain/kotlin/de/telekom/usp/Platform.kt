package de.telekom.usp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform