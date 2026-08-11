/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.datamodel

import de.telekom.usp.Path
import de.telekom.usp.ResolvedPath
import de.telekom.usp.isParameter
import de.telekom.usp.last
import kotlinx.coroutines.flow.SharedFlow

/**
 * Allows storage and retrieval of USP data model parameters.
 */
interface DataModel {

    /**
     * A flow receiving events whenever data in this [DataModel] has been added and changed.
     */
    val updates: SharedFlow<InstanceObject>

    /**
     * A flow receiving events whenever data in this [DataModel] has been deleted.
     */
    val deletes: SharedFlow<ResolvedPath>

    /**
     * Returns the values stored in this data model for the specified object path.
     *
     * @param path the path to retrieve its data from
     * @param maxDepth the maximum number of path elements to descend to, when returning the result.
     *        A [maxDepth] of 0 means, only return the table data of [path]. When for example searching
     *        for `Device.IP.Interface.` with a maxDepth of `1`, this would also return the parameters
     *        of `Device.IP.Interface.1.` and `Device.IP.Interface.2.` (when existing of course).
     */
    suspend fun read(path: ResolvedPath, maxDepth: Int = 0): List<InstanceObject>

    /**
     * Returns the direct children in this data model for the specified object path
     *
     * @param path the path to retrieve its children from
     * @return a list of path elements, which are all direct children of the specified path, but not
     *         the path itself. Hence for `Device.IP.Interface.` it will return for example
     *         `Device.IP.Interface.1.` and `Device.IP.Interface.2.`
     */
    suspend fun directChildren(path: ResolvedPath): List<ResolvedPath>

    /**
     * Reads a single parameter from this data model.
     *
     * @param parameter a path for which [Path.isParameter] returns `true`
     * @return the value of the specified parameter or `null` if it doesn't exist
     * @throws IllegalArgumentException when [parameter] does not denote a parameter path
     */
    suspend fun readParameter(parameter: ResolvedPath): String? {
        if (!parameter.isParameter) {
            throw IllegalArgumentException("Expected a parameter path: '$parameter'")
        }

        val path = parameter.dropLast(1)
        val param = parameter.last().toString()

        return read(path).firstOrNull()?.rows?.get(param)
    }

    /**
     * Defines the table data of the paths specified in `data` overwriting any existing values. If
     * any path in `data` does not exist yet in this data model, it will be created.
     *
     * @see [add]
     */
    suspend fun set(vararg data: InstanceObject)

    /**
     * Adds the table data of the paths specified in `data` leaving any other existing values in
     * this path unchanged. If any path in `data` does not exist yet in this data model, it will be
     * created.
     *
     * @see [set]
     */
    suspend fun add(vararg data: InstanceObject)

    /**
     * Deletes the specified path and all of its sub-paths from this data model.
     */
    suspend fun delete(path: ResolvedPath): Boolean
}