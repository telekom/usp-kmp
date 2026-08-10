/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp

object Versions {

    const val MOST_RECENT = "1.3"

    private val supported = listOf("1.0", "1.1", "1.2", MOST_RECENT)

    fun isSupported(version: String) = supported.contains(version)

    override fun toString(): String {
        return "Supported versions: ${supported.joinToString()}"
    }
}