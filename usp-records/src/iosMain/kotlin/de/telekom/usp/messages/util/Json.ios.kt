/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.messages.util

import okio.BufferedSink
import okio.BufferedSource
import kotlin.reflect.KClass

internal actual object Json {

    actual fun <T : Any> decodeFrom(json: String, type: KClass<T>) : T {
        TODO("Decoding classes from Json is not supported on iOS")
    }

    actual fun <T : Any> decodeFrom(source: BufferedSource, type: KClass<T>) : T {
        TODO("Decoding classes from Json is not supported on iOS")
    }

    actual fun <T : Any> encodeTo(value: T): String {
        TODO("Encoding classes from Json is not supported on iOS")
    }

    actual fun <T : Any> encodeTo(sink: BufferedSink, value: T) {
        TODO("Encoding classes from Json is not supported on iOS")
    }
}