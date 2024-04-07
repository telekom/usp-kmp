package de.telekom.usp.datamodel

import de.telekom.usp.ResolvedPath

/**
 * Allows storage and retrieval of USP data model parameters.
 */
interface DataModel {

    /**
     * Returns the values stored in this data model for the specified path.
     *
     * @param path the path to retrieve its data from
     * @param maxDepth the maximum number of path elements to descend to, when returning the result.
     *        A [maxDepth] of 0 means, only return the table data of [path]. When for example searching
     *        for `Device.IP.Interface.` with a maxDepth of `1`, this would also return the parameters
     *        of `Device.IP.Interface.1.` and `Device.IP.Interface.2.` (when existing of course).
     */
    suspend fun read(path: ResolvedPath, maxDepth: Int = 0): List<InstanceObject>

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