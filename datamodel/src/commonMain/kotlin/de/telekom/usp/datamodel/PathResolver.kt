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

    // Gets recursively called to resolved all path element in the unresolved paths
    private suspend fun resolveAll(unresolved: List<Path>): List<ResolvedPath> {
        return unresolved.flatMap { path ->
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
                    throw UnsupportedOperationException("Resolving reference following is not yet supported ($this)")
                }
                resolveWildcardAt(index)
            }

            is PathElement.Expression -> {
                resolveExpressionAt(index)
            }

            else -> {
                throw IllegalArgumentException("Unexpected path element: '$element' of type ${element::class.qualifiedName}")
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
}