package de.telekom.usp.datamodel

import de.telekom.usp.Path
import de.telekom.usp.ResolvedPath

interface PathResolver {

    /**
     * Return all resolved paths matching the specified path for the data model of this.
     *
     * @return a list of resolved paths. The list will be empty, if no paths matched the specified
     *         path name
     */
    suspend fun resolve(path: Path): List<ResolvedPath>
}