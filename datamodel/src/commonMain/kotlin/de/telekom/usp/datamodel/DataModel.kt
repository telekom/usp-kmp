package de.telekom.usp.datamodel

import de.telekom.usp.ResolvedPath

interface DataModel {

    suspend fun read(path: ResolvedPath): List<InstanceObject>

    suspend fun set(vararg data: InstanceObject)

    suspend fun add(vararg data: InstanceObject)

    suspend fun delete(vararg data: InstanceObject)
}