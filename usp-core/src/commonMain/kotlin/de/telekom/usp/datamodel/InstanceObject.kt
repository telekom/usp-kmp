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