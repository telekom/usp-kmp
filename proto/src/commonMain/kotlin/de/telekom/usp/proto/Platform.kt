package de.telekom.usp.proto

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform