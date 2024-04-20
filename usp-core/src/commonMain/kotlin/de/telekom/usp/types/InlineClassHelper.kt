package de.telekom.usp.types

public inline fun packInts(val1: Int, val2: Int): Long = val1.toLong().shl(32) or (val2.toLong() and
        0xFFFFFFFF)

public inline fun unpackInt1(`value`: Long): Int = value.shr(32).toInt()

public inline fun unpackInt2(`value`: Long): Int = value.and(0xFFFFFFFF).toInt()
