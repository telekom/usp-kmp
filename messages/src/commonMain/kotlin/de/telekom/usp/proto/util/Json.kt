package de.telekom.usp.proto.util

import okio.BufferedSink
import okio.BufferedSource
import kotlin.reflect.KClass

internal expect object Json {

    fun <T : Any> decodeFrom(json: String, type: KClass<T>) : T

    fun <T : Any> decodeFrom(source: BufferedSource, type: KClass<T>) : T

    fun <T : Any> encodeTo(value: T) : String

    fun <T : Any> encodeTo(sink: BufferedSink, value: T)
}