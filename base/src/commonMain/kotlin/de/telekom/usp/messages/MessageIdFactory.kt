package de.telekom.usp.messages

import de.telekom.usp.util.AtomicCounter

object MessageIdFactory {

    private val id = AtomicCounter(0)

    fun next() = id.next().toString()
}