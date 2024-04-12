package de.telekom.usp.datamodel

import de.telekom.usp.Device
import de.telekom.usp.ResolvedPath
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

    init {
        val devicePath = Device.toPath()
        if (!fileSystem.exists(devicePath)) {
            fileSystem.createDirectories(devicePath)
        }
    }

    override suspend fun read(path: ResolvedPath, maxDepth: Int): List<InstanceObject> {
        TODO("Not yet implemented")
    }

    override suspend fun directChildren(path: ResolvedPath): List<ResolvedPath> {
        val dir = path.toPath()
        return if (!fileSystem.exists(dir)) {
            emptyList()
        } else {
            fileSystem.list(dir).mapNotNull { file ->
                if (file.isDirectory) {
                    path + file.name
                } else {
                    null
                }
            }
        }
    }

    override suspend fun set(vararg data: InstanceObject) {
        for (instance in data) {
            val path = instance.path.toPath()
            ensurePathExists(path)
            writeTo(instance.path.toPath(), instance.rows)
        }
    }

    override suspend fun add(vararg data: InstanceObject) {
        for (instance in data) {
            val path = instance.path.toPath()
            ensurePathExists(path)
            writeTo(path, readFrom(path) + instance.rows)
        }
    }

    override suspend fun delete(path: ResolvedPath): Boolean {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeTo(directory: Path, data: Map<String, String>) {
        fileSystem.sink(directory.resolve(DATA_FILE)).buffer().use { sink ->
            Json.encodeToBufferedSink(data, sink)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readFrom(directory: Path): Map<String, String> {
        fileSystem.source(directory.resolve(DATA_FILE)).buffer().use { source ->
            return Json.decodeFromBufferedSource<Map<String, String>>(source)
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

    private val Path.isDirectory: Boolean
        get() = fileSystem.metadata(this).isDirectory

    companion object {
        private val DATA_FILE = "data.json".toPath()
    }
}