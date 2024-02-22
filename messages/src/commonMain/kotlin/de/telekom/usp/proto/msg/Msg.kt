package de.telekom.usp.proto.msg

val Msg.isRequest: Boolean
    get() = body?.request != null

val Msg.isResponse: Boolean
    get() = body?.response != null

val Msg.isError: Boolean
    get() = body?.error != null

val Msg.id: String
    get() = header_?.msg_id ?: "UNDEFINED"

val Msg.error: Error
    get() {
        requireType(Header.MsgType.ERROR)
        return body!!.error!!
    }

val Msg.getRequest: Get
    get() {
        requireType(Header.MsgType.GET)
        return body!!.request!!.get_!!
    }

val Msg.getSupportedDmRequest: GetSupportedDM
    get() {
        requireType(Header.MsgType.GET_SUPPORTED_DM)
        return body!!.request!!.get_supported_dm!!
    }

val Msg.getInstancesRequest: GetInstances
    get() {
        requireType(Header.MsgType.GET_INSTANCES)
        return body!!.request!!.get_instances!!
    }

val Msg.getSupportedProtocolRequest: GetSupportedProtocol
    get() {
        requireType(Header.MsgType.GET_SUPPORTED_PROTO)
        return body!!.request!!.get_supported_protocol!!
    }

val Msg.setRequest: Set
    get() {
        requireType(Header.MsgType.SET)
        return body!!.request!!.set_!!
    }

val Msg.addRequest: Add
    get() {
        requireType(Header.MsgType.ADD)
        return body!!.request!!.add!!
    }

val Msg.deleteRequest: Delete
    get() {
        requireType(Header.MsgType.DELETE)
        return body!!.request!!.delete!!
    }

val Msg.operateRequest: Operate
    get() {
        requireType(Header.MsgType.OPERATE)
        return body!!.request!!.operate!!
    }

val Msg.notifyRequest: Notify
    get() {
        requireType(Header.MsgType.NOTIFY)
        return body!!.request!!.notify!!
    }

val Msg.registerRequest: Register
    get() {
        requireType(Header.MsgType.REGISTER)
        return body!!.request!!.register!!
    }

val Msg.deregisterRequest: Deregister
    get() {
        requireType(Header.MsgType.DEREGISTER)
        return body!!.request!!.deregister!!
    }

val Msg.getResponse: GetResp
    get() {
        requireType(Header.MsgType.GET_RESP)
        return body!!.response!!.get_resp!!
    }

val Msg.getSupportedDmResponse: GetSupportedDMResp
    get() {
        requireType(Header.MsgType.GET_SUPPORTED_DM_RESP)
        return body!!.response!!.get_supported_dm_resp!!
    }

val Msg.getInstancesResponse: GetInstancesResp
    get() {
        requireType(Header.MsgType.GET_INSTANCES_RESP)
        return body!!.response!!.get_instances_resp!!
    }

val Msg.getSupportedProtocolResponse: GetSupportedProtocolResp
    get() {
        requireType(Header.MsgType.GET_SUPPORTED_PROTO_RESP)
        return body!!.response!!.get_supported_protocol_resp!!
    }

val Msg.setResponse: SetResp
    get() {
        requireType(Header.MsgType.SET_RESP)
        return body!!.response!!.set_resp!!
    }

val Msg.addResponse: AddResp
    get() {
        requireType(Header.MsgType.ADD_RESP)
        return body!!.response!!.add_resp!!
    }

val Msg.deleteResponse: DeleteResp
    get() {
        requireType(Header.MsgType.DELETE_RESP)
        return body!!.response!!.delete_resp!!
    }

val Msg.operateResponse: OperateResp
    get() {
        requireType(Header.MsgType.OPERATE_RESP)
        return body!!.response!!.operate_resp!!
    }

val Msg.notifyResponse: NotifyResp
    get() {
        requireType(Header.MsgType.NOTIFY_RESP)
        return body!!.response!!.notify_resp!!
    }

val Msg.registerResponse: RegisterResp
    get() {
        requireType(Header.MsgType.REGISTER_RESP)
        return body!!.response!!.register_resp!!
    }

val Msg.deregisterResponse: DeregisterResp
    get() {
        requireType(Header.MsgType.DEREGISTER_RESP)
        return body!!.response!!.deregister_resp!!
    }

/**
 * Determines whether this message is a response of the specified message.
 *
 * @return `true` when this is an error or a response message and it contains the same ID as the
 *         specified parameter, `false` otherwise
 */
fun Msg.isResponseOf(request: Msg): Boolean {
    // R-MSG.9 responses must contain the same ID as the originating request
    return (isResponse || isError) && header_?.msg_id == request.header_?.msg_id
}

fun Msg.requireType(type: Header.MsgType) {
    if (header_?.msg_type != type) {
        throw IllegalStateException("Wrong type, requested: $type, actual: ${header_?.msg_type} for $this")
    }
}
