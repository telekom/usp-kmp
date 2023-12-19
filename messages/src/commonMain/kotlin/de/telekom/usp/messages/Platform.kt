package de.telekom.usp.messages

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform