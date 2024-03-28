package de.telekom.usp.mtp.mqtt

import kotlin.jvm.JvmInline

@JvmInline
value class Topic(val value: String) {

    fun isNotEmpty() = value.isNotEmpty()

    val isWildcard: Boolean
        get() = value.endsWith("/#") || value.contains("/+")

    override fun toString(): String {
        return value
    }
}