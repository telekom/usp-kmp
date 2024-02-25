package de.telekom.usp.util

import kotlin.concurrent.AtomicLong

actual class AtomicSequence actual constructor(value: Long) {

    private val value = AtomicLong(value)

    actual fun next() = value.addAndGet(1)
}