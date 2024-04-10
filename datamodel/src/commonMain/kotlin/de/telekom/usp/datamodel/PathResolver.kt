package de.telekom.usp.datamodel

import de.telekom.usp.Path
import de.telekom.usp.ResolvedPath

class PathResolver(private val dataModel: DataModel) {

    /**
     * Return all resolved paths matching the specified path for the data model of this.
     *
     * @return a list of resolved paths. The list will be empty, if no paths matched the specified
     *         path name
     */
    suspend fun resolve(path: Path): List<ResolvedPath> {
        if (path.isResolved) {
            return listOf(path.asResolvedPath())
        }

        val work = mutableListOf(path)
        resolvePathAt(work)

        return work.map { it.asResolvedPath() }
    }

    private fun resolvePathAt(unresolved: MutableList<Path>) {
        for (path in unresolved) {

        }
    }
}