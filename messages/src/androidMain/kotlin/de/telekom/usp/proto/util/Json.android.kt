package de.telekom.usp.proto.util

import com.squareup.moshi.Moshi
import com.squareup.wire.WireJsonAdapterFactory
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
}