package de.telekom.usp.proto.msg

val Msg.id: String
    get() = header_?.msg_id ?: "UNDEFINED"

@Suppress("UNCHECKED_CAST")
fun <T> Msg.bodyAs(type: Header.MsgType): T {
    return when (type) {
        // Requests
        Header.MsgType.GET -> body!!.request!!.get_ as T
        Header.MsgType.GET_SUPPORTED_DM -> body!!.request!!.get_supported_dm as T
        Header.MsgType.GET_INSTANCES -> body!!.request!!.get_instances as T
        Header.MsgType.SET -> body!!.request!!.set_ as T
        Header.MsgType.ADD -> body!!.request!!.add as T
        Header.MsgType.DELETE -> body!!.request!!.delete as T
        Header.MsgType.OPERATE -> body!!.request!!.operate as T
        Header.MsgType.NOTIFY -> body!!.request!!.notify as T
        Header.MsgType.GET_SUPPORTED_PROTO -> body!!.request!!.get_supported_protocol as T
        Header.MsgType.REGISTER -> body!!.request!!.register as T
        Header.MsgType.DEREGISTER -> body!!.request!!.deregister as T
        // Responses
        Header.MsgType.GET_RESP -> body!!.response!!.get_resp as T
        Header.MsgType.GET_SUPPORTED_DM_RESP -> body!!.response!!.get_supported_dm_resp as T
        Header.MsgType.GET_INSTANCES_RESP -> body!!.response!!.get_instances_resp as T
        Header.MsgType.SET_RESP -> body!!.response!!.set_resp as T
        Header.MsgType.ADD_RESP -> body!!.response!!.add_resp as T
        Header.MsgType.DELETE_RESP -> body!!.response!!.delete_resp as T
        Header.MsgType.OPERATE_RESP -> body!!.response!!.operate_resp as T
        Header.MsgType.NOTIFY_RESP -> body!!.response!!.notify_resp as T
        Header.MsgType.GET_SUPPORTED_PROTO_RESP -> body!!.response!!.get_supported_protocol_resp as T
        Header.MsgType.REGISTER_RESP -> body!!.response!!.register_resp as T
        Header.MsgType.DEREGISTER_RESP -> body!!.response!!.delete_resp as T
        // Error
        Header.MsgType.ERROR -> body!!.error as T
    }
}

