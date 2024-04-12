package de.telekom.usp.datamodel

import de.telekom.usp.Device
import de.telekom.usp.IP
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.fakefilesystem.FakeFileSystem
import okio.use
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FileDataModelTest {

    private val rows = mutableMapOf(
        "Enable" to "true",
        "Alias" to "eth0",
        "Status" to "Up",
        "Name" to "",
        "LastChange" to "0",
    )

    private lateinit var fileSystem: FakeFileSystem

    private lateinit var model: FileDataModel

    private val root = "data-model".toPath()

    @BeforeTest
    fun setup() {
        fileSystem = FakeFileSystem()
        model = FileDataModel(fileSystem, root)
    }

    @AfterTest
    fun tearDown() {
        fileSystem.checkNoOpenFiles()
    }

    @Test
    fun `only device directory exists after creation`() {
        val allPaths = setOf("/data-model".toPath(), "/data-model/Device.".toPath())
        assertEquals(allPaths, fileSystem.allPaths)
    }

    @Test
    fun `direct children returns proper values`() = runTest {
        fileSystem.createDirectories(root.resolve("Device./IP.".toPath()))
        fileSystem.createDirectories(root.resolve("Device./DeviceInfo.".toPath()))
        fileSystem.createDirectories(root.resolve("Device./1.".toPath()))

        val children = model.directChildren(Device)
        assertEquals(setOf(Device + "IP.", Device + "DeviceInfo.", Device + "1."), children.toSet())
    }

    @Test
    fun `set creates json file with data`() = runTest {
        model.set(InstanceObject(IP, rows))

        val dataFile = fileSystem.allPaths.first { it.name == "data.json" }
        assertEquals(rows, readRows(dataFile))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readRows(path: Path): Map<String, String> {
        fileSystem.source(path).buffer().use { source ->
            return Json.decodeFromBufferedSource<Map<String, String>>(source)
        }
    }
}