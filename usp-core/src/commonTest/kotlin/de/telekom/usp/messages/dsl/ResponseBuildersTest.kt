/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.messages.dsl

import de.telekom.usp.*
import de.telekom.usp.messages.proto.GetSupportedDMResp.*
import de.telekom.usp.messages.proto.Header
import kotlin.test.*

class ResponseBuildersTest {

    @Test
    fun `create GetResp message`() {
        val resp = GetResp("id-1") {
            result("Device.") {
                addResolvedPath("Device.WiFi.") {
                    params["abc"] = "123"
                    params["def"] = "456"
                }
                addResolvedPath(Path("Device.Network.")) {
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

    @Test
    fun `create GetSupportedDMResp message`() {
        val resp = GetSupportedDMResp("msg-1") {
            result("Device.Wifi.", "urn:broadband-forum-org:tr-181-2-12-0") {
                addObject("Device.Wifi.ResetCounter", ObjAccessType.OBJ_ADD_ONLY, true) {
                    addCommand("cmd-1", CmdType.CMD_SYNC) {
                        inputArgs.add("cmd-input")
                        outputArgs.add("cmd-output")
                    }
                    addEvent("evt-1") {
                        args.add("evt-arg")
                    }
                    addParam(
                        "param-1",
                        ParamAccessType.PARAM_READ_ONLY,
                        ParamValueType.PARAM_BOOLEAN,
                        ValueChangeType.VALUE_CHANGE_ALLOWED
                    )
                }
            }
        }

        assertMessageType(Header.MsgType.GET_SUPPORTED_DM_RESP, resp)
        assertNotNull(resp.body!!.response!!.get_supported_dm_resp)
        assertEquals("msg-1", resp.header_!!.msg_id)

        val results = resp.body!!.response!!.get_supported_dm_resp!!.req_obj_results
        assertEquals(1, results.size)
        assertEquals(0, results[0].err_code)
        assertEquals("", results[0].err_msg)
        assertEquals("Device.Wifi.", results[0].req_obj_path)
        assertEquals("urn:broadband-forum-org:tr-181-2-12-0", results[0].data_model_inst_uri)

        val objects = results[0].supported_objs
        assertEquals(1, objects.size)
        assertEquals("Device.Wifi.ResetCounter", objects[0].supported_obj_path)
        assertEquals(ObjAccessType.OBJ_ADD_ONLY, objects[0].access)
        assertTrue(objects[0].is_multi_instance)
        assertEquals(1, objects[0].supported_commands.size)
        assertEquals("cmd-1", objects[0].supported_commands[0].command_name)
        assertEquals(CmdType.CMD_SYNC, objects[0].supported_commands[0].command_type)
        assertEquals("cmd-input", objects[0].supported_commands[0].input_arg_names[0])
        assertEquals("cmd-output", objects[0].supported_commands[0].output_arg_names[0])

        assertEquals(1, objects[0].supported_events.size)
        assertEquals("evt-1", objects[0].supported_events[0].event_name)
        assertEquals("evt-arg", objects[0].supported_events[0].arg_names[0])

        assertEquals(1, objects[0].supported_params.size)
        assertEquals("param-1", objects[0].supported_params[0].param_name)
        assertEquals(ParamAccessType.PARAM_READ_ONLY, objects[0].supported_params[0].access)
        assertEquals(ParamValueType.PARAM_BOOLEAN, objects[0].supported_params[0].value_type)
        assertEquals(
            ValueChangeType.VALUE_CHANGE_ALLOWED,
            objects[0].supported_params[0].value_change
        )
    }

    @Test
    fun `create GetInstancesResp message`() {
        val resp = GetInstancesResp("42") {
            result("Device.Wifi.") {
                addCurrInstance("Relative.") {
                    uniqueKeys["key-1"] = "value-1"
                }
                addCurrInstance("Relative2.") {
                    uniqueKeys["key-2"] = "value-2"
                }
            }
        }

        assertMessageType(Header.MsgType.GET_INSTANCES_RESP, resp)
        assertNotNull(resp.body!!.response!!.get_instances_resp)
        assertEquals("42", resp.header_!!.msg_id)

        val results = resp.body!!.response!!.get_instances_resp!!.req_path_results
        assertEquals(1, results.size)
        assertEquals("Device.Wifi.", results[0].requested_path)
        assertEquals(2, results[0].curr_insts.size)
        assertEquals("Relative.", results[0].curr_insts[0].instantiated_obj_path)
        assertEquals("value-1", results[0].curr_insts[0].unique_keys["key-1"])
        assertEquals("Relative2.", results[0].curr_insts[1].instantiated_obj_path)
        assertEquals("value-2", results[0].curr_insts[1].unique_keys["key-2"])
    }

    @Test
    fun `create SetResp message`() {
        val resp = SetResp("set-1") {
            result("Device.") {
                success {
                    addInstance("Device.Wifi.") {
                        params["param1"] = "value1"
                        addError("just another", InternalError)
                    }
                }
            }
            result("Device.NAT.") {
                failure(MessageNotSupported) {
                    addInstance("Device.Wifi.") {
                        addError("param-1", RequestDenied)
                    }
                }
            }
        }

        assertMessageType(Header.MsgType.SET_RESP, resp)
        assertNotNull(resp.body!!.response!!.set_resp)
        assertEquals("set-1", resp.header_!!.msg_id)

        val results = resp.body!!.response!!.set_resp!!.updated_obj_results
        assertEquals(2, results.size)
        assertNotNull(results[0].oper_status)
        assertNotNull(results[0].oper_status!!.oper_success)
        assertNull(results[0].oper_status!!.oper_failure)
        assertEquals(1, results[0].oper_status!!.oper_success!!.updated_inst_results.size)

        val instances = results[0].oper_status!!.oper_success!!.updated_inst_results
        assertEquals("Device.Wifi.", instances[0].affected_path)
        assertEquals("value1", instances[0].updated_params["param1"])
        assertEquals(InternalError.code, instances[0].param_errs[0].err_code)

        assertNotNull(results[1].oper_status)
        assertNotNull(results[1].oper_status!!.oper_failure)
        assertNull(results[1].oper_status!!.oper_success)
        assertEquals(1, results[1].oper_status!!.oper_failure!!.updated_inst_failures.size)
        assertEquals(MessageNotSupported.code, results[1].oper_status!!.oper_failure!!.err_code)

        val failures = results[1].oper_status!!.oper_failure!!.updated_inst_failures
        assertEquals("Device.Wifi.", failures[0].affected_path)
        assertEquals(RequestDenied.code, failures[0].param_errs[0].err_code)
        assertEquals("param-1", failures[0].param_errs[0].param_)
    }

    @Test
    fun `create AddResp message`() {
        val resp = AddResp("add-1") {
            result("Device.") {
                success("Device.WiFi.") {
                    uniqueKeys["created"] = "yes"
                    addError("error-1", RequestDenied)
                }
            }
            result("Device.NAT.") {
                failure(MessageNotSupported)
            }
        }

        assertMessageType(Header.MsgType.ADD_RESP, resp)
        assertNotNull(resp.body!!.response!!.add_resp)
        assertEquals("add-1", resp.header_!!.msg_id)

        val results = resp.body!!.response!!.add_resp!!.created_obj_results
        assertEquals(2, results.size)
        assertEquals("Device.", results[0].requested_path)
        assertNotNull(results[0].oper_status!!.oper_success)
        assertNull(results[0].oper_status!!.oper_failure)
        assertEquals("Device.WiFi.", results[0].oper_status!!.oper_success!!.instantiated_path)
        assertEquals("yes", results[0].oper_status!!.oper_success!!.unique_keys["created"])
        assertEquals(
            RequestDenied.code,
            results[0].oper_status!!.oper_success!!.param_errs[0].err_code
        )

        assertEquals("Device.NAT.", results[1].requested_path)
        assertNotNull(results[1].oper_status!!.oper_failure)
        assertNull(results[1].oper_status!!.oper_success)
        assertEquals(MessageNotSupported.code, results[1].oper_status!!.oper_failure!!.err_code)
    }

    @Test
    fun `create DeleteResp message`() {
        val resp = DeleteResp("del-1") {
            result("Device.") {
                success("Device.Wifi.") {
                    addPath("Device.Wifi.1.")
                    addError("Device.Wifi.1.", ParameterActionFailed)
                }
            }
            result("Device.NAT.") {
                failure(InvalidType)
            }
        }

        assertMessageType(Header.MsgType.DELETE_RESP, resp)
        assertNotNull(resp.body!!.response!!.delete_resp)
        assertEquals("del-1", resp.header_!!.msg_id)

        val results = resp.body!!.response!!.delete_resp!!.deleted_obj_results
        assertEquals(2, results.size)
        assertEquals("Device.", results[0].requested_path)
        assertNotNull(results[0].oper_status!!.oper_success)
        assertNull(results[0].oper_status!!.oper_failure)
        assertEquals("Device.Wifi.1.", results[0].oper_status!!.oper_success!!.affected_paths[0])
        assertEquals(
            ParameterActionFailed.code,
            results[0].oper_status!!.oper_success!!.unaffected_path_errs[0].err_code
        )

        assertEquals("Device.NAT.", results[1].requested_path)
        assertNull(results[1].oper_status!!.oper_success)
        assertNotNull(results[1].oper_status!!.oper_failure)
    }

    @Test
    fun `create OperateResp message`() {
        val resp = OperateResp("oper-1") {
            result("Device.SelfTestDiagnostics()") {
                requestObjectPath("Device.LocalAgent.Request.1.")
            }
            result("Device.WiFi.") {
                commandFailure(ParameterActionFailed)
            }
            result("Device.Network.") {
                requestedOutputArgs["test"] = "123"
            }
        }

        assertMessageType(Header.MsgType.OPERATE_RESP, resp)
        assertNotNull(resp.body!!.response!!.operate_resp)
        assertEquals("oper-1", resp.header_!!.msg_id)

        val results = resp.body!!.response!!.operate_resp!!.operation_results
        assertEquals(3, results.size)
        assertEquals("Device.SelfTestDiagnostics()", results[0].executed_command)
        assertEquals("Device.LocalAgent.Request.1.", results[0].req_obj_path)
        assertEquals("Device.WiFi.", results[1].executed_command)
        assertEquals(ParameterActionFailed.code, results[1].cmd_failure!!.err_code)
        assertEquals("Device.Network.", results[2].executed_command)
        assertEquals("123", results[2].req_output_args!!.output_args["test"])
    }

    @Test
    fun `create NotifyResp message`() {
        val resp = NotifyResp("notify-1", "subscription_1")

        assertMessageType(Header.MsgType.NOTIFY_RESP, resp)
        assertNotNull(resp.body!!.response!!.notify_resp)
        assertEquals("notify-1", resp.header_!!.msg_id)
        assertEquals("subscription_1", resp.body!!.response!!.notify_resp!!.subscription_id)
    }

    @Test
    fun `create GetSupportedProtocolResp message`() {
        val resp = GetSupportedProtocolResp("proto-1", "1.2,1.3")

        assertMessageType(Header.MsgType.GET_SUPPORTED_PROTO_RESP, resp)
        assertNotNull(resp.body!!.response!!.get_supported_protocol_resp)
        assertEquals("proto-1", resp.header_!!.msg_id)
        assertEquals(
            "1.2,1.3",
            resp.body!!.response!!.get_supported_protocol_resp!!.agent_supported_protocol_versions
        )
    }

    @Test
    fun `create RegisterResp message`() {
        val resp = RegisterResp("resp-1") {
            result("Device.") {
                registeredPath("Device.Network.")
            }
            result("Device.WiFi.") {
                failure(ParameterActionFailed)
            }
        }

        assertMessageType(Header.MsgType.REGISTER_RESP, resp)
        assertNotNull(resp.body!!.response!!.register_resp)
        assertEquals("resp-1", resp.header_!!.msg_id)

        val results = resp.body!!.response!!.register_resp!!.registered_path_results
        assertEquals(2, results.size)

        assertEquals("Device.", results[0].requested_path)
        assertNotNull(results[0].oper_status!!.oper_success)
        assertNull(results[0].oper_status!!.oper_failure)
        assertEquals("Device.Network.", results[0].oper_status!!.oper_success!!.registered_path)

        assertEquals("Device.WiFi.", results[1].requested_path)
        assertNotNull(results[1].oper_status!!.oper_failure)
        assertNull(results[1].oper_status!!.oper_success)
        assertEquals(ParameterActionFailed.code, results[1].oper_status!!.oper_failure!!.err_code)
    }

    @Test
    fun `create DeregisterResp message`() {
        val resp = DeregisterResp("de-1") {
            result("Device.") {
                addDeregisteredPath("Device.Network.")
                addDeregisteredPath("Device.WiFi.")
            }
            result("Device.NAT.") {
                failure(ParameterActionFailed)
            }
        }

        assertMessageType(Header.MsgType.DEREGISTER_RESP, resp)
        assertNotNull(resp.body!!.response!!.deregister_resp)
        assertEquals("de-1", resp.header_!!.msg_id)

        val results = resp.body!!.response!!.deregister_resp!!.deregistered_path_results
        assertEquals(2, results.size)

        assertEquals("Device.", results[0].requested_path)
        assertNotNull(results[0].oper_status!!.oper_success)
        assertNull(results[0].oper_status!!.oper_failure)
        assertEquals(
            "Device.Network.",
            results[0].oper_status!!.oper_success!!.deregistered_path[0]
        )
        assertEquals("Device.WiFi.", results[0].oper_status!!.oper_success!!.deregistered_path[1])

        assertEquals("Device.NAT.", results[1].requested_path)
        assertNotNull(results[1].oper_status!!.oper_failure)
        assertNull(results[1].oper_status!!.oper_success)
        assertEquals(ParameterActionFailed.code, results[1].oper_status!!.oper_failure!!.err_code)
    }
}







