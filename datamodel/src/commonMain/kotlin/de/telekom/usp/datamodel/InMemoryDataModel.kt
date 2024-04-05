package de.telekom.usp.datamodel

import de.telekom.usp.Device
import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath

class InMemoryDataModel : DataModel {

    private val root = Node(Device.asResolvedPath())

    override suspend fun read(path: ResolvedPath): List<InstanceObject> {
        checkPath(path)
        TODO("Not yet implemented")
    }

    override suspend fun set(vararg data: InstanceObject) {
        for (instance in data) {
            findOrCreateNode(instance.path).setRows(instance.rows)
        }
        println(root)
    }

    override suspend fun add(vararg data: InstanceObject) {
        for (instance in data) {
            findOrCreateNode(instance.path).addRows(instance.rows)
        }
        println(root)
    }

    override suspend fun delete(vararg data: InstanceObject) {
        TODO("Not yet implemented")
    }

    private fun checkPath(path: ResolvedPath) {
        require(path.first() == Device) { "Path must start with Device. but starts with: '${path.first()}'" }
        require(path.last() is PathElement.Object) { "Only object paths allowed, found: '$path'" }
    }

    private fun findOrCreateNode(path: ResolvedPath): Node {
        if (path.size == 1) {
            return root
        }

        var current = root
        for (index in 1 until path.elements.size) {
            val pathElement = path[index] as PathElement.Object
            val child = current.findChildNode(pathElement)
            current = child ?: current.addChild(Node(path.subPath(0, index + 1)))
        }
        return current
    }

    private class Node(val path: ResolvedPath) : Comparable<Node> {

        val rows = mutableMapOf<String, String>()

        val value: PathElement.Object
            get() = path.last() as PathElement.Object

        private val children: MutableList<Node> = mutableListOf()

        fun addChild(node: Node): Node {
            children.add(node)
            children.sort()
            return node
        }

        fun removeChild(node: Node) {
            val index = children.binarySearch(node)
            if (index >= 0) {
                children.removeAt(index)
            }
        }

        fun findChildNode(pathElement: PathElement.Object): Node? {
            var low = 0
            var high = children.size - 1

            while (low <= high) {
                val mid = (low + high).ushr(1) // safe from overflows
                val midVal = children[mid]
                val cmp = midVal.value.text.compareTo(pathElement.text)

                if (cmp < 0)
                    low = mid + 1
                else if (cmp > 0)
                    high = mid - 1
                else
                    return midVal // key found
            }
            return null  // key not found
        }

        fun setRows(rows: Map<String, String>) {
            this.rows.clear()
            this.rows.putAll(rows)
        }

        fun addRows(rows: Map<String, String>) {
            this.rows.putAll(rows)
        }

        fun printNode(indent: String): String {
            return buildString {
                append(indent)
                if (this@Node.value != Device.first()) {
                    append("- ")
                }
                append(path)
                append(":\n")
                rows.forEach { row ->
                    append(indent)
                    append("  ")
                    row.toYaml(this)
                }
                children.forEach { child ->
                    append(child.printNode("  $indent"))
                }
            }
        }

        override fun compareTo(other: Node): Int {
            return value.text.compareTo(other.value.text)
        }

        override fun toString(): String {
            return printNode("")
        }
    }
}

private fun Map.Entry<String, String>.toYaml(str: StringBuilder) {
    str.append("- ")
    str.append(key)
    str.append(": ")
    if (value == "true" || value == "false" || value.toLongOrNull() != null) {
        str.append(value)
    } else {
        str.append('"')
        str.append(value)
        str.append('"')
    }
    str.append("\n")
}
