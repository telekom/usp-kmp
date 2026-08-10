/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.util

import okio.ByteString

private const val OFFSET_BASIS = 0x811C9DC5.toInt()
private const val FNV_PRIME = 0x1000193

fun ByteString.fnv1aHash(): Int {
    var hash = OFFSET_BASIS
    for (i in 0 until size) {
        hash = (this[i].toInt() xor hash) * FNV_PRIME
    }
    return hash
}

fun ByteArray.fnv1aHash(): Int {
    var hash = OFFSET_BASIS
    for (b in this) {
        hash = (b.toInt() xor hash) * FNV_PRIME
    }
    return hash
}

fun String.fnv1aHash(): Int {
    return encodeToByteArray().fnv1aHash()
}