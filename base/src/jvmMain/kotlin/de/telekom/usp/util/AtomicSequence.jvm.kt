package de.telekom.usp.util

import java.util.concurrent.atomic.AtomicLong

actual class AtomicSequence actual constructor(value: Long) {

    private val value = AtomicLong(value)

    actual fun next() = value.incrementAndGet()
}