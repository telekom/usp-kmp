/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp

import kotlin.jvm.JvmInline

@JvmInline
value class AuthorityId(private val authority: String) {

    override fun toString(): String {
        return authority
    }
}