/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.telekom.usp.datamodel

sealed class TerminalPath {

    data class Parameter(val name: String, val description: String?, val access: String) :
        TerminalPath() {

    }

    data class Command(val name: String, val description: String?, val async: Boolean) :
        TerminalPath() {
        init {
            if (!name.endsWith("()")) {
                throw IllegalArgumentException("Command name must end with (): '$name'")
            }
        }
    }

    data class Event(val name: String, val description: String?, val id: String?) : TerminalPath() {
        init {
            if (!name.endsWith("!")) {
                throw IllegalArgumentException("Event name must end with !: '$name'")
            }
        }
    }
}