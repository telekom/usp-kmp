package de.telekom.usp.datamodel

import co.touchlab.kermit.Logger
import de.telekom.usp.Device
import de.telekom.usp.ResolvedPath
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

class FileDataModel(private val fileSystem: FileSystem, private val directory: Path) : DataModel {

    private val _updates = MutableSharedFlow<InstanceObject>()
    override val updates: SharedFlow<InstanceObject>
        get() = _updates

    private val _deletes = MutableSharedFlow<ResolvedPath>()
    override val deletes: SharedFlow<ResolvedPath>
        get() = _deletes

    private val mutex = Mutex()

    init {
        val devicePath = Device.toPath()
        if (!fileSystem.exists(devicePath)) {
            fileSystem.createDirectories(devicePath)
        }
    }

    override suspend fun read(path: ResolvedPath, maxDepth: Int): List<InstanceObject> {
        mutex.withLock {
            return buildList {
                collectChildData(this, path.toPath(), maxDepth)
            }
        }
    }

    override suspend fun directChildren(path: ResolvedPath): List<ResolvedPath> {
        mutex.withLock {
            return path.toPath().childDirectories().map { path.add(it.name) }
        }
    }

    override suspend fun set(vararg data: InstanceObject) {
        mutex.withLock {
            for (instance in data) {
                instance.path.toPath().let { path ->
                    writeTo(path, instance.rows)
                }
                _updates.emit(instance)
            }
        }
    }

    override suspend fun add(vararg data: InstanceObject) {
        mutex.withLock {
            for (instance in data) {
                instance.path.toPath().let { path ->
                    writeTo(path, readFrom(path) + instance.rows)
                }
                _updates.emit(instance)
            }
        }
    }

    override suspend fun delete(path: ResolvedPath): Boolean {
        mutex.withLock {
            return path.toPath().let { dir ->
                if (fileSystem.exists(dir)) {
                    fileSystem.deleteRecursively(dir)
                    _deletes.emit(path)
                    true
                } else {
                    false
                }
            }
        }
    }

    // --- Helper methods --------------------------------------------------------------------------

    private fun collectChildData(
        instances: MutableList<InstanceObject>,
        parent: Path,
        depth: Int
    ) {
        val resolvedPath = parent.relativeTo(directory).toResolvedPath()
        val rows = readFrom(parent)
        instances.add(InstanceObject(resolvedPath, rows))

        if (depth > 0) {
            parent.childDirectories().forEach { dir ->
                collectChildData(instances, dir, depth - 1)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeTo(directory: Path, data: Map<String, String>) {
        ensurePathExists(directory)
        val file = directory.resolve(DATA_FILE)

        fileSystem.sink(file).buffer().use { sink ->
            Json.encodeToBufferedSink(data, sink)
            Logger.d { "Encoded ${data.size} row(s) to $file" }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readFrom(directory: Path): Map<String, String> {
        val file = directory.resolve(DATA_FILE)

        if (fileSystem.exists(file)) {
            fileSystem.source(file).buffer().use { source ->
                val rows = Json.decodeFromBufferedSource<Map<String, String>>(source)
                Logger.d { "Decoded ${rows.size} row(s) from $file" }
                return rows
            }
        } else {
            return emptyMap()
        }
    }

    private fun ensurePathExists(path: Path) {
        if (!fileSystem.exists(path)) {
            fileSystem.createDirectories(path)
        }
    }

    private fun ResolvedPath.toPath(): Path {
        return elements.fold(directory) { path, element ->
            path.resolve(element.text)
        }
    }

    private fun Path.toResolvedPath(): ResolvedPath {
        return ResolvedPath(toString().replace("/", ""))
    }

    private fun Path.childDirectories(): List<Path> {
        return if (fileSystem.exists(this)) {
            fileSystem.list(this).filter { it.isDirectory }
        } else {
            emptyList()
        }
    }

    private val Path.isDirectory: Boolean
        get() = fileSystem.metadata(this).isDirectory


    companion object {

        private val DATA_FILE = "data.json".toPath()
    }
}