package de.telekom.usp.messages

object MessageIdFactory {

    private var id = 1L

    fun next() = id++.toString()
}