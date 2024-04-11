package de.telekom.usp.datamodel

import co.touchlab.kermit.Logger
import de.telekom.usp.Path
import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath
import de.telekom.usp.datamodel.ExpressionComponent.Companion.matches

class PathResolver(private val model: DataModel) {

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
        return resolveAll(listOf(path))
    }

    private suspend fun resolveAll(unresolved: List<Path>): List<ResolvedPath> {
        return unresolved.flatMap { path ->
            val index = path.elements.indexOfFirst { !it.isResolved }

            if (index == -1) {
                listOf(path.asResolvedPath())
            } else {
                resolveAll(resolveAt(index, path))
            }
        }
    }

    private suspend fun resolveAt(index: Int, path: Path): List<Path> {
        return when (val element = path.elements[index]) {
            is PathElement.Object -> {
                resolveWildcardAt(index, path)
            }

            is PathElement.Expression -> {
                resolveExpressionAt(index, path)
            }

            else -> {
                throw IllegalArgumentException("Unexpected path element: '$element' of type ${element::class.qualifiedName}")
            }
        }
    }

    private suspend fun resolveWildcardAt(index: Int, path: Path): List<Path> {
        // Index is the first non resolved path, hence subPath will be resolved!
        val query = path.subPath(0, index).asResolvedPath()

        return model.directChildren(query).map { child ->
            path.replace(index, child.last())
        }
    }


    private suspend fun resolveExpressionAt(index: Int, path: Path): List<Path> {
        return try {
            val expressions = ExpressionParser(path[index] as PathElement.Expression).parse()
            val basePath = path.subPath(0, index).asResolvedPath()

            return model.directChildren(basePath).mapNotNull { child ->
                val matches = expressions.all { expression ->
                    val param = child + expression.relpath.toString()
                    val value = model.readParameter(param)
                    value matches expression
                }
                if (matches) {
                    path.replace(index, child.last())
                } else {
                    null
                }
            }
        } catch (ex: ExpressionParserException) {
            Logger.e(throwable = ex) { "Error parsing expression in '$path' at position ${index + 1}" }
            emptyList()
        }
    }

    private fun Path.replace(index: Int, replacement: PathElement): Path {
        return mapIndexed { i, element -> if (index == i) replacement else element }
    }
}