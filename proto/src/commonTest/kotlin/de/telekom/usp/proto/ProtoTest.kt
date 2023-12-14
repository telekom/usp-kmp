package de.telekom.usp.proto

import de.telekom.usp.DeviceInfo
import de.telekom.usp.proto.msg.Get
import de.telekom.usp.toStrings
import kotlin.test.Test

class ProtoTest {

    @Test
    fun `test something`() {
        val get = Get(param_paths = listOf(DeviceInfo).toStrings(), max_depth = 1)
        val bytes = get.encode()
        println(bytes.joinToString())

        println(Get.ADAPTER.decode(bytes))
    }
}