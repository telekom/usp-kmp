package de.telekom.usp.messages.util

import com.squareup.moshi.Moshi
import com.squareup.wire.WireJsonAdapterFactory
import okio.BufferedSink
import okio.BufferedSource
import kotlin.reflect.KClass

internal actual object Json {

    private val moshi = Moshi.Builder().add(WireJsonAdapterFactory()).build()

    actual fun <T : Any> decodeFrom(json: String, type: KClass<T>) : T {
        return moshi.adapter(type.java).fromJson(json)!!
    }

    actual fun <T : Any> decodeFrom(source: BufferedSource, type: KClass<T>) : T {
        return moshi.adapter(type.java).fromJson(source)!!
    }

    actual fun <T : Any> encodeTo(value: T): String {
        return moshi.adapter<T>(value::class.java).indent("  ").toJson(value)
    }

    actual fun <T : Any> encodeTo(sink: BufferedSink, value: T) {
        return moshi.adapter<T>(value::class.java).indent("  ").toJson(sink, value)
    }
}