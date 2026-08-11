/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.mtp.ws

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.logging.*

class KtorKermitBridge(level: LogLevel) : io.ktor.client.plugins.logging.Logger {

    private val logImpl: (String) -> Unit = when (level) {
        LogLevel.ALL, LogLevel.HEADERS, LogLevel.BODY -> {
            { Logger.d(tag = "Ktor", messageString = it) }
        }

        LogLevel.INFO -> {
            { Logger.i(tag = "Ktor", messageString = it) }
        }

        LogLevel.NONE -> {
            { }
        }
    }

    override fun log(message: String) {
        logImpl(message)
    }
}