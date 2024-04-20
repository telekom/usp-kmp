package de.telekom.usp.util

/**
 * A thread safe sequence of Long values.
 */
expect class AtomicSequence(value: Long) {

    fun next(): Long
}