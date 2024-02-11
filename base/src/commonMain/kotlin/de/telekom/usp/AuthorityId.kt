package de.telekom.usp

import kotlin.jvm.JvmInline

@JvmInline
value class AuthorityId(private val authority: String) {

    override fun toString(): String {
        return authority
    }
}