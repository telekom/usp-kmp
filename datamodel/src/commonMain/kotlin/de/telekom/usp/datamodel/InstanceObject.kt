package de.telekom.usp.datamodel

import de.telekom.usp.ResolvedPath

/**
 * Representation of a USP data model parameter with its table data.
 */
data class InstanceObject(val path: ResolvedPath, val rows: Map<String, String>) {

    init {
        require(!path.isTerminal) { "Instance object paths must not be terminal" }
    }
}