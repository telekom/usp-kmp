package de.telekom.usp.util

import kotlin.concurrent.AtomicInt

actual class AtomicCounter actual constructor(value: Int) {

    private val value = AtomicInt(value)

    actual fun next() = value.addAndGet(1)
}