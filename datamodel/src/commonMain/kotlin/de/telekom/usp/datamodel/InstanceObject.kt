package de.telekom.usp.datamodel

import de.telekom.usp.ResolvedPath

data class InstanceObject(val path: ResolvedPath, val rows: Map<String, String>) {
}