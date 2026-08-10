/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.datamodel

import de.telekom.usp.ResolvedPath
import de.telekom.usp.isTerminal

/**
 * Representation of a USP data model parameter with its table data.
 */
data class InstanceObject(val path: ResolvedPath, val rows: Map<String, String>) {

    init {
        require(!path.isTerminal) { "Instance object paths cannot be terminal: '$path'" }
    }
}