package de.telekom.usp.messages.dsl

import de.telekom.usp.Path
import de.telekom.usp.messages.proto.Header
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ResponseBuildersTest {

    @Test
    fun `create GetResp message`() {
        val resp = GetResp("id-1") {
            result("Device.") {
                resolvedPath("Device.WiFi.") {
                    params["abc"] = "123"
                    params["def"] = "456"
                }
                resolvedPath(Path("Device.Network.")) {
                    params["eth0"] = "789"
                }
            }
        }

        assertMessageType(Header.MsgType.GET_RESP, resp)
        assertNotNull(resp.body!!.response!!.get_resp)
        assertEquals("id-1", resp.header_!!.msg_id)

        val results = resp.body!!.response!!.get_resp!!.req_path_results
        assertEquals(1, results.size)
        assertEquals(0, results[0].err_code)
        assertEquals("", results[0].err_msg)
        assertEquals("Device.", results[0].requested_path)
        assertEquals(2, results[0].resolved_path_results.size)
        assertEquals("Device.WiFi.", results[0].resolved_path_results[0].resolved_path)
        assertEquals("Device.Network.", results[0].resolved_path_results[1].resolved_path)
        assertEquals("123", results[0].resolved_path_results[0].result_params["abc"])
        assertEquals("456", results[0].resolved_path_results[0].result_params["def"])
        assertEquals("789", results[0].resolved_path_results[1].result_params["eth0"])
    }
}