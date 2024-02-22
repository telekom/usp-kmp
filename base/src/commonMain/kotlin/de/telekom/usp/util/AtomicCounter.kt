package de.telekom.usp.util

/**
 * A thread safe counter
 */
expect class AtomicCounter(value: Int) {

    fun next(): Int
}