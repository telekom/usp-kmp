package de.telekom.usp.proto.util

import okio.BufferedSource
import kotlin.reflect.KClass

internal expect object Json {

    fun <T : Any> decodeFrom(json: String, type: KClass<T>) : T

    fun <T : Any> decodeFrom(source: BufferedSource, type: KClass<T>) : T
}