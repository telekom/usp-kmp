package de.telekom.usp.messages.dsl

import de.telekom.usp.MessageNotSupported
import de.telekom.usp.messages.proto.Header
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RequestBuildersTest {

    private val samplePaths = arrayOf("Device.DeviceInfo.", "Device.UserInterface.")

    @Test
    fun `create message with custom message ID`() {
        val get = Get {
            messageId = "test-message-id"
        }

        assertEquals("test-message-id", get.header_!!.msg_id)
    }

    @Test
    fun `create Get message`() {
        val get = Get {
            maxDepth = 42
            paths(*samplePaths)
        }

        assertMessageType(Header.MsgType.GET, get)
        assertNotNull(get.body!!.request!!.get_)
        assertEquals(42, get.body!!.request!!.get_!!.max_depth)
        assertEquals(samplePaths.toList(), get.body!!.request!!.get_!!.param_paths)
    }

    @Test
    fun `create Set message`() {
        val set = Set {
            allowPartial = false
            path(samplePaths[0]) {
                params["param1"] = "value1" required true
            }
        }

        assertMessageType(Header.MsgType.SET, set)
        assertNotNull(set.body!!.request!!.set_)
        assertFalse(set.body!!.request!!.set_!!.allow_partial)
        assertEquals(samplePaths[0], set.body!!.request!!.set_!!.update_objs[0].obj_path)
        assertEquals("param1", set.body!!.request!!.set_!!.update_objs[0].param_settings[0].param_)
        assertEquals("value1", set.body!!.request!!.set_!!.update_objs[0].param_settings[0].value_)
    }

    @Test
    fun `create Add message`() {
        val add = Add {
            allowPartial = false
            path(samplePaths[0]) {
                params["param1"] = "value1" required true
            }
        }

        assertMessageType(Header.MsgType.ADD, add)
        assertNotNull(add.body!!.request!!.add)
        assertFalse(add.body!!.request!!.add!!.allow_partial)
        assertEquals(samplePaths[0], add.body!!.request!!.add!!.create_objs[0].obj_path)
        assertEquals("param1", add.body!!.request!!.add!!.create_objs[0].param_settings[0].param_)
        assertEquals("value1", add.body!!.request!!.add!!.create_objs[0].param_settings[0].value_)
    }

    @Test
    fun `create Delete message`() {
        val delete = Delete {
            allowPartial = false
            paths(samplePaths[0])
        }

        assertMessageType(Header.MsgType.DELETE, delete)
        assertNotNull(delete.body!!.request!!.delete)
        assertFalse(delete.body!!.request!!.delete!!.allow_partial)
        assertEquals(samplePaths[0], delete.body!!.request!!.delete!!.obj_paths[0])
    }

    @Test
    fun `create Register message`() {
        val register = Register {
            allowPartial = false
            paths(samplePaths[0])
        }

        assertMessageType(Header.MsgType.REGISTER, register)
        assertNotNull(register.body!!.request!!.register)
        assertFalse(register.body!!.request!!.register!!.allow_partial)
        assertEquals(samplePaths[0], register.body!!.request!!.register!!.reg_paths[0].path)
    }

    @Test
    fun `create Deregister message`() {
        val deregister = Deregister {
            paths(samplePaths[0])
        }

        assertMessageType(Header.MsgType.DEREGISTER, deregister)
        assertNotNull(deregister.body!!.request!!.deregister)
        assertEquals(samplePaths[0], deregister.body!!.request!!.deregister!!.paths[0])
    }

    @Test
    fun `create Operate message`() {
        assertFailsWith(IllegalArgumentException::class) {
            Operate("Device.MissingCommand.", "cmd_key") { }
        }

        val operate = Operate("Device.Command()", "cmd_key") {
            args["arg1"] = "value1"
        }

        assertMessageType(Header.MsgType.OPERATE, operate)
        assertNotNull(operate.body!!.request!!.operate)
        assertEquals("cmd_key", operate.body!!.request!!.operate!!.command_key)
        assertEquals("value1", operate.body!!.request!!.operate!!.input_args["arg1"])
    }

    @Test
    fun `create Notify message`() {
        val notify = Notify("subscription_1") {
            sendResponse = false
        }

        assertMessageType(Header.MsgType.NOTIFY, notify)
        assertNotNull(notify.body!!.request!!.notify)
        assertEquals("subscription_1", notify.body!!.request!!.notify!!.subscription_id)
        assertFalse(notify.body!!.request!!.notify!!.send_resp)

        assertFailsWith(IllegalArgumentException::class) {
            Notify("subscription_1") {
                event("Device.MissingEvent.", "name") { }
            }
        }

        val event = Notify("subscription_1") {
            event("Device.SampleEvent!", "event_name_1") {
                params["param1"] = "value1"
            }
        }
        assertNotNull(event.body!!.request!!.notify!!.event)
        assertEquals("Device.SampleEvent!", event.body!!.request!!.notify!!.event!!.obj_path)
        assertEquals("event_name_1", event.body!!.request!!.notify!!.event!!.event_name)
        assertEquals("value1", event.body!!.request!!.notify!!.event!!.params["param1"])

        val valueChange = Notify("subscription_1") {
            valueChange("Device.", "xyz")
        }
        assertNotNull(valueChange.body!!.request!!.notify!!.value_change)
        assertEquals("xyz", valueChange.body!!.request!!.notify!!.value_change!!.param_value)
        assertEquals("Device.", valueChange.body!!.request!!.notify!!.value_change!!.param_path)

        val objectCreation = Notify("subscription_1") {
            objectCreation(samplePaths[0]) {
                uniqueKeys["key_1"] = "unique_1"
                uniqueKeys["key_2"] = "unique_2"
            }
        }
        assertNotNull(objectCreation.body!!.request!!.notify!!.obj_creation)
        assertEquals(
            samplePaths[0],
            objectCreation.body!!.request!!.notify!!.obj_creation!!.obj_path
        )
        assertEquals(
            "unique_1",
            objectCreation.body!!.request!!.notify!!.obj_creation!!.unique_keys["key_1"]
        )
        assertEquals(
            "unique_2",
            objectCreation.body!!.request!!.notify!!.obj_creation!!.unique_keys["key_2"]
        )

        val objectDeletion = Notify("subscription_1") {
            objectDeletion(samplePaths[0])
        }
        assertNotNull(objectDeletion.body!!.request!!.notify!!.obj_deletion)
        assertEquals(
            samplePaths[0],
            objectDeletion.body!!.request!!.notify!!.obj_deletion!!.obj_path
        )

        val operationComplete1 = Notify("subscription_1") {
            operationComplete(samplePaths[0], "command_name1", "key_1") {
                commandFailure = MessageNotSupported.toPair()
            }
        }
        assertNotNull(operationComplete1.body!!.request!!.notify!!.oper_complete)
        assertEquals(
            samplePaths[0],
            operationComplete1.body!!.request!!.notify!!.oper_complete!!.obj_path
        )
        assertEquals(
            "command_name1",
            operationComplete1.body!!.request!!.notify!!.oper_complete!!.command_name
        )
        assertEquals(
            "key_1",
            operationComplete1.body!!.request!!.notify!!.oper_complete!!.command_key
        )
        assertEquals(
            MessageNotSupported.code,
            operationComplete1.body!!.request!!.notify!!.oper_complete!!.cmd_failure!!.err_code
        )
        assertEquals(
            MessageNotSupported.name,
            operationComplete1.body!!.request!!.notify!!.oper_complete!!.cmd_failure!!.err_msg
        )

        val operationComplete2 = Notify("subscription_1") {
            operationComplete(samplePaths[0], "command_name1", "key_1") {
                outputArgs["arg_1"] = "value_1"
            }
        }
        assertNotNull(operationComplete2.body!!.request!!.notify!!.oper_complete)
        assertEquals(
            samplePaths[0],
            operationComplete2.body!!.request!!.notify!!.oper_complete!!.obj_path
        )
        assertEquals(
            "command_name1",
            operationComplete2.body!!.request!!.notify!!.oper_complete!!.command_name
        )
        assertEquals(
            "key_1",
            operationComplete2.body!!.request!!.notify!!.oper_complete!!.command_key
        )
        assertEquals(
            "value_1",
            operationComplete2.body!!.request!!.notify!!.oper_complete!!.req_output_args!!.output_args["arg_1"]
        )

        val onBoardRequest = Notify("subscription_1") {
            onBoardRequest("oui_1", "product_2", "serial_3", "v_4")
        }
        assertNotNull(onBoardRequest.body!!.request!!.notify!!.on_board_req)
        assertEquals("oui_1", onBoardRequest.body!!.request!!.notify!!.on_board_req!!.oui)
        assertEquals(
            "product_2",
            onBoardRequest.body!!.request!!.notify!!.on_board_req!!.product_class
        )
        assertEquals(
            "serial_3",
            onBoardRequest.body!!.request!!.notify!!.on_board_req!!.serial_number
        )
        assertEquals(
            "v_4",
            onBoardRequest.body!!.request!!.notify!!.on_board_req!!.agent_supported_protocol_versions
        )
    }

    @Test
    fun `create GetSupportedDm message`() {
        val getSupportedDm = GetSupportedDm {
            firstLevelOnly = false
            returnCommands = true
            returnEvents = false
            returnParams = true
        }

        assertMessageType(Header.MsgType.GET_SUPPORTED_DM, getSupportedDm)
        assertNotNull(getSupportedDm.body!!.request!!.get_supported_dm)
        assertFalse(getSupportedDm.body!!.request!!.get_supported_dm!!.first_level_only)
        assertTrue(getSupportedDm.body!!.request!!.get_supported_dm!!.return_commands)
        assertFalse(getSupportedDm.body!!.request!!.get_supported_dm!!.return_events)
        assertTrue(getSupportedDm.body!!.request!!.get_supported_dm!!.return_params)
    }

    @Test
    fun `create GetSupportedProtocol message`() {
        val getSupportedProtocol = GetSupportedProtocol("v_77")

        assertMessageType(Header.MsgType.GET_SUPPORTED_PROTO, getSupportedProtocol)
        assertNotNull(getSupportedProtocol.body!!.request!!.get_supported_protocol)
        assertEquals(
            "v_77",
            getSupportedProtocol.body!!.request!!.get_supported_protocol!!.controller_supported_protocol_versions
        )
    }

    @Test
    fun `create GetInstances message`() {
        val getInstances = GetInstances {
            firstLevelOnly = false
            paths(*samplePaths)
        }

        assertMessageType(Header.MsgType.GET_INSTANCES, getInstances)
        assertNotNull(getInstances.body!!.request!!.get_instances)
        assertEquals(samplePaths.toList(), getInstances.body!!.request!!.get_instances!!.obj_paths)
    }
}
