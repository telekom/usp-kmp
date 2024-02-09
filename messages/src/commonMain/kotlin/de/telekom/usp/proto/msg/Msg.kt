package de.telekom.usp.proto.msg

import kotlin.Error

val Msg.id: String
    get() = header_?.msg_id ?: "UNDEFINED"

/**
 * Returns the body of this Msg as the specified type `T`. The type `T` must be one of the request,
 * response or the Error type. Example:
 * ```kotlin
 * val getResp = msg.bodyAs<GetResp>()
 * ```
 */
inline fun <reified T> Msg.bodyAs(): T {
    return when (T::class) {
        // Requests
        Get::class -> body!!.request!!.get_ as T
        GetSupportedDM::class -> body!!.request!!.get_supported_dm as T
        GetInstances::class -> body!!.request!!.get_instances as T
        Set::class -> body!!.request!!.set_ as T
        Add::class -> body!!.request!!.add as T
        Delete::class -> body!!.request!!.delete as T
        Operate::class -> body!!.request!!.operate as T
        Notify::class -> body!!.request!!.notify as T
        GetSupportedProtocol::class -> body!!.request!!.get_supported_protocol as T
        Register::class -> body!!.request!!.register as T
        Deregister::class -> body!!.request!!.deregister as T
        // Responses
        GetResp::class -> body!!.response!!.get_resp as T
        GetSupportedDMResp::class -> body!!.response!!.get_supported_dm_resp as T
        GetInstancesResp::class -> body!!.response!!.get_instances_resp as T
        SetResp::class -> body!!.response!!.set_resp as T
        AddResp::class -> body!!.response!!.add_resp as T
        DeleteResp::class -> body!!.response!!.delete_resp as T
        OperateResp::class -> body!!.response!!.operate_resp as T
        NotifyResp::class -> body!!.response!!.notify_resp as T
        GetSupportedProtocolResp::class -> body!!.response!!.get_supported_protocol_resp as T
        RegisterResp::class -> body!!.response!!.register_resp as T
        DeregisterResp::class -> body!!.response!!.delete_resp as T
        // Error
        Error::class -> body!!.error as T
        // Failed
        else -> throw IllegalArgumentException("Unknown Msg type: ${T::class}")
    }
}

