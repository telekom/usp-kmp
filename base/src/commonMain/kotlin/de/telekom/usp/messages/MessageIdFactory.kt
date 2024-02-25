package de.telekom.usp.messages

import de.telekom.usp.messages.MessageIdFactory.next
import de.telekom.usp.util.AtomicSequence

/**
 * Creates unique message IDs for every call of [next]. This object is thread safe.
 */
object MessageIdFactory {

    private val id = AtomicSequence(0)

    fun next() = id.next().toString()
}