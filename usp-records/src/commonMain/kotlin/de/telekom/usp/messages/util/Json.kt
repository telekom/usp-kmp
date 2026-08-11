/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.messages.util

import okio.BufferedSink
import okio.BufferedSource
import kotlin.reflect.KClass

internal expect object Json {

    fun <T : Any> decodeFrom(json: String, type: KClass<T>) : T

    fun <T : Any> decodeFrom(source: BufferedSource, type: KClass<T>) : T

    fun <T : Any> encodeTo(value: T) : String

    fun <T : Any> encodeTo(sink: BufferedSink, value: T)
}