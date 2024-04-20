package de.telekom.usp.datamodel

import co.touchlab.kermit.Logger
import de.telekom.usp.Path
import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath
import de.telekom.usp.datamodel.ExpressionComponent.Companion.matches

class PathResolverImpl(private val model: DataModel) : PathResolver {

    /**
     * Return all resolved paths matching the specified path for the data model of this.
     *
     * @return a list of resolved paths. The list will be empty, if no paths matched the specified
     *         path name
     */
    override suspend fun resolve(path: Path): List<ResolvedPath> {
        if (path.isResolved) {
            return listOf(path.asResolvedPath())
        }
        return resolveAll(listOf(path))
    }

    // Gets recursively called to resolved all path element in the unresolved paths
    private suspend fun resolveAll(paths: List<Path>): List<ResolvedPath> {
        return paths.flatMap { path ->
            val index = path.elements.indexOfFirst { !it.isResolved }

            if (index == -1) {
                listOf(path.asResolvedPath())
            } else {
                resolveAll(path.resolveAt(index))
            }
        }
    }

    private suspend fun Path.resolveAt(index: Int): List<Path> {
        return when (val element = elements[index]) {
            is PathElement.Object -> {
                if (element.refFollow != null) {
                    resolveReferenceFollowingAt(index)
                } else if (element.instance == 0) {
                    resolveWildcardAt(index)
                } else {
                    throw Error("Unexpected unresolved Object ($element) in '$this'")
                }
            }

            is PathElement.Expression -> {
                resolveExpressionAt(index)
            }

            else -> {
                throw Error("Unexpected unresolved path element: '$element' of type ${element::class.qualifiedName}")
            }
        }
    }

    private suspend fun Path.resolveWildcardAt(index: Int): List<Path> {
        // The index points to the first non resolved path, hence subPath() will return a resolved path!
        val basePath = subPath(0, index).asResolvedPath()

        return model.directChildren(basePath).map { child ->
            replace(index, child.last())
        }
    }

    private suspend fun Path.resolveReferenceFollowingAt(index: Int): List<Path> {
        val refFollow = (elements[index] as PathElement.Object).refFollow!!
        val refPath = (subPath(0, index) + refFollow.name).asResolvedPath()
        val remainder = subPath(index + 1, elements.size).toString()
        val refValues = model.readParameter(refPath).toItems()

        return refValues.mapIndexedNotNull { i, refValue ->
            if (refFollow.followAll || i == refFollow.itemNumber - 1) {
                ResolvedPath(refValue) + remainder
            } else {
                null
            }
        }
    }

    private suspend fun Path.resolveExpressionAt(index: Int): List<Path> {
        return try {
            // The index points to the first non resolved path, hence subPath will be a resolved path!
            val basePath = subPath(0, index).asResolvedPath()
            val expressions = ExpressionParser(this[index] as PathElement.Expression).parse()

            return model.directChildren(basePath).mapNotNull { child ->
                val matches = expressions.all { expression ->
                    val param = child + expression.relpath.toString()
                    val value = model.readParameter(param)

                    value matches expression
                }

                if (matches) {
                    replace(index, child.last())
                } else {
                    null
                }
            }
        } catch (ex: ExpressionParserException) {
            Logger.e(throwable = ex) { "Error parsing expression in '$this' at position ${index + 1}" }
            emptyList()
        }
    }

    private fun Path.replace(index: Int, replacement: PathElement): Path {
        return mapIndexed { i, element -> if (index == i) replacement else element }
    }

    private fun String?.toItems(): List<String> {
        return this?.split(",")?.map { it.trim() } ?: emptyList()
    }
}