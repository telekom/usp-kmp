package de.telekom.usp.messages

import de.telekom.usp.DeviceInfo
import de.telekom.usp.messages.util.Json
import de.telekom.usp.proto.msg.Get
import de.telekom.usp.proto.msg.GetSupportedProtocol
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

    @Test
    fun `test json to proto`() {
        val json = "{ \"controller_supported_protocol_versions\": \"1.0,1.1,1.2\" }"
        val supported = Json.decodeFrom(json, GetSupportedProtocol::class)
        println(supported)
    }
}
