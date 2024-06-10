package de.telekom.usp.datamodel

import co.touchlab.kermit.Logger
import de.telekom.usp.Device
import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath
import de.telekom.usp.first
import de.telekom.usp.get
import de.telekom.usp.last
import de.telekom.usp.size
import de.telekom.usp.startsWithDevice
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class InMemoryDataModel : DataModel {

    private val _updates = MutableSharedFlow<InstanceObject>()
    override val updates: SharedFlow<InstanceObject>
        get() = _updates

    private val _deletes = MutableSharedFlow<ResolvedPath>()
    override val deletes: SharedFlow<ResolvedPath>
        get() = _deletes

    private val root = Node(Device)

    private val mutex = Mutex()

    override suspend fun read(path: ResolvedPath, maxDepth: Int): List<InstanceObject> {
        mutex.withLock {
            return buildList {
                val node = findNode(path)
                if (node != null) {
                    collectChildData(this, node, maxDepth)
                }
            }
        }
    }

    override suspend fun directChildren(path: ResolvedPath): List<ResolvedPath> {
        mutex.withLock {
            return findNode(path)?.children?.map { it.path } ?: emptyList()
        }
    }

    override suspend fun set(vararg data: InstanceObject) {
        mutex.withLock {
            for (instance in data) {
                findOrCreateNode(instance.path).setRows(instance.rows)
                _updates.emit(instance)
            }
        }
    }

    override suspend fun add(vararg data: InstanceObject) {
        mutex.withLock {
            for (instance in data) {
                findOrCreateNode(instance.path).addRows(instance.rows)
                _updates.emit(instance)
            }
        }
    }

    override suspend fun delete(path: ResolvedPath): Boolean {
        checkPath(path)
        return mutex.withLock {
            if (path == Device) {
                Logger.w("Cannot delete the root path (Device.) from a data model")
                return false
            }
            val parent = findNode(path.dropLast(1))

            val success = parent?.removeChild(path.last() as PathElement.Object) ?: false
            if (success) {
                _deletes.emit(path)
            }
            success
        }
    }

    override fun toString(): String {
        return root.toString()
    }

    // --- Helper methods --------------------------------------------------------------------------

    private fun checkPath(path: ResolvedPath) {
        require(path.startsWithDevice()) { "Path must start with Device., but starts with: '${path.first()}'" }
        require(path.last() is PathElement.Object) { "Only object paths allowed, found: '$path'" }
    }

    private fun findNode(path: ResolvedPath): Node? {
        checkPath(path)

        return if (path.size == 1) {
            root
        } else {
            root.findChildNodeFor(path, 1)
        }
    }

    private fun findOrCreateNode(path: ResolvedPath): Node {
        checkPath(path)

        return if (path.size == 1) {
            root
        } else {
            root.findOrCreateChildNodeFor(path, 1)
        }
    }

    private fun collectChildData(instances: MutableList<InstanceObject>, parent: Node, depth: Int) {
        instances.add(InstanceObject(parent.path, parent.rows))

        if (depth > 0) {
            parent.children.forEach { child ->
                collectChildData(instances, child, depth - 1)
            }
        }
    }

    protected class Node(val path: ResolvedPath) : Comparable<Node> {

        private val _rows = mutableMapOf<String, String>()
        val rows: Map<String, String>
            get() = _rows

        private val value: PathElement.Object
            get() = path.last() as PathElement.Object

        private val _children: MutableList<Node> = mutableListOf()
        val children: List<Node>
            get() = _children

        private fun addChild(node: Node): Node {
            _children.add(node)
            _children.sort()
            return node
        }

        fun removeChild(toRemove: PathElement.Object): Boolean {
            return searchChildNode(toRemove)?.let {
                _children.remove(it)
                true
            } ?: false
        }

        fun findOrCreateChildNodeFor(path: ResolvedPath, fromIndex: Int): Node {
            val child = searchChildNode(path[fromIndex] as PathElement.Object)
                ?: addChild(Node(path.subPath(0, fromIndex + 1)))

            return if (fromIndex == path.size - 1) {
                child
            } else {
                child.findOrCreateChildNodeFor(path, fromIndex + 1)
            }
        }

        fun findChildNodeFor(path: ResolvedPath, fromIndex: Int): Node? {
            val child = searchChildNode(path[fromIndex] as PathElement.Object)

            return if (fromIndex == path.size - 1) {
                child
            } else {
                child?.findChildNodeFor(path, fromIndex + 1)
            }
        }

        private fun searchChildNode(pathElement: PathElement.Object): Node? {
            var low = 0
            var high = _children.size - 1

            while (low <= high) {
                val mid = (low + high).ushr(1) // safe from overflows
                val midVal = _children[mid]
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
            this._rows.clear()
            this._rows.putAll(rows)
        }

        fun addRows(rows: Map<String, String>) {
            this._rows.putAll(rows)
        }

        fun printNode(indent: String): String {
            return buildString {
                append(indent)
                if (this@Node.value != Device.first()) {
                    append("- ")
                }
                append(value)
                append(":\n")
                _rows.forEach { row ->
                    append(indent)
                    append("  ")
                    row.toYaml(this)
                }
                _children.forEach { child ->
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
