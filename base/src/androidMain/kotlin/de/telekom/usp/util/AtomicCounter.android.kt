package de.telekom.usp.util

import java.util.concurrent.atomic.AtomicInteger

actual class AtomicCounter actual constructor(value: Int) {

    private val value = AtomicInteger(value)

    actual fun next() = value.incrementAndGet()
}