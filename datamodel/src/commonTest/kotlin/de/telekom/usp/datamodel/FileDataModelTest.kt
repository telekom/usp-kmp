package de.telekom.usp.datamodel

import de.telekom.usp.Device
import de.telekom.usp.DeviceInfo
import de.telekom.usp.IP
import de.telekom.usp.WiFi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.fakefilesystem.FakeFileSystem
import okio.use
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileDataModelTest {

    private val rows = mutableMapOf(
        "Enable" to "true",
        "Alias" to "eth0",
        "Name" to "",
        "LastChange" to "0",
    )

    private lateinit var fileSystem: FakeFileSystem

    private lateinit var model: FileDataModel

    private val root = "/data-model".toPath()

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
        val allPaths = setOf(root, root.resolve("Device.".toPath()))
        assertEquals(allPaths, fileSystem.allPaths)
    }

    @Test
    fun `direct children returns child paths`() = runTest {
        assertTrue(model.directChildren(Device).isEmpty())

        fileSystem.createDirectories(root.resolve("Device./IP.".toPath()))
        fileSystem.createDirectories(root.resolve("Device./DeviceInfo.".toPath()))
        fileSystem.createDirectories(root.resolve("Device./1.".toPath()))

        val children = model.directChildren(Device)
        assertEquals(setOf(Device + "IP.", Device + "DeviceInfo.", Device + "1."), children.toSet())
    }

    @Test
    fun `read traverses the tree properly`() = runTest {
        writeRows(root.resolve("Device./IP./data.json".toPath()), rows)
        writeRows(root.resolve("Device./IP./Interface./data.json".toPath()), rows)
        writeRows(root.resolve("Device./DeviceInfo./data.json".toPath()), rows)

        val level0 = model.read(Device, 0)
        assertTrue(level0.size == 1)
        assertEquals(Device, level0[0].path)
        assertTrue(level0[0].rows.isEmpty())

        val level1 = model.read(Device, 1)
        assertTrue(level1.size == 3)
        assertEquals(rows, level1.first { it.path == IP }.rows)
        assertEquals(rows, level1.first { it.path == DeviceInfo }.rows)
        assertTrue(level1.first { it.path == Device }.rows.isEmpty())

        val level2 = model.read(Device, Int.MAX_VALUE)
        assertTrue(level2.size == 4)
        assertEquals(rows, level2.first { it.path == IP }.rows)
        assertEquals(rows, level2.first { it.path == IP + "Interface." }.rows)
        assertEquals(rows, level2.first { it.path == DeviceInfo }.rows)
        assertTrue(level2.first { it.path == Device }.rows.isEmpty())
    }

    @Test
    fun `set creates json file with all data`() = runTest {
        repeat(2) { // Check overwriting of data files
            model.set(InstanceObject(IP, rows))

            val dataFile = fileSystem.allPaths.first { it.name == "data.json" }
            assertEquals(rows, readRows(dataFile))
        }
    }

    @Test
    fun `add does not remove existing data`() = runTest {
        val existing = mapOf("pre-existing" to "yes")
        model.add(InstanceObject(IP, existing))
        assertEquals(existing, model.read(IP, 0).first().rows)

        model.add(InstanceObject(IP, rows))
        assertEquals(rows + existing, model.read(IP, 0).first().rows)
    }

    @Test
    fun `delete removes directory`() = runTest {
        val path = root.resolve("Device./IP.".toPath())
        fileSystem.createDirectories(path)
        writeRows(path.resolve("data.json"), rows)

        val deleted = model.delete(IP)
        assertTrue(deleted)
        assertFalse(fileSystem.exists(path))
    }

    @Test
    fun `deleting non existing directory returns false`() = runTest {
        assertFalse(model.delete(WiFi))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readRows(path: Path): Map<String, String> {
        fileSystem.source(path).buffer().use { source ->
            return Json.decodeFromBufferedSource<Map<String, String>>(source)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeRows(path: Path, rows: Map<String, String>) {
        fileSystem.createDirectories(path.parent!!)
        fileSystem.sink(path).buffer().use { sink ->
            Json.encodeToBufferedSink(rows, sink)
        }
    }
}