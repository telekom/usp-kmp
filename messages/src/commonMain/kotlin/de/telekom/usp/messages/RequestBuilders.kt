@file:Suppress("FunctionName", "unused", "MemberVisibilityCanBePrivate")

package de.telekom.usp.messages

import de.telekom.usp.proto.msg.Add
import de.telekom.usp.proto.msg.Body
import de.telekom.usp.proto.msg.Delete
import de.telekom.usp.proto.msg.Get
import de.telekom.usp.proto.msg.GetInstances
import de.telekom.usp.proto.msg.GetSupportedDM
import de.telekom.usp.proto.msg.Header
import de.telekom.usp.proto.msg.Msg
import de.telekom.usp.proto.msg.Operate
import de.telekom.usp.proto.msg.Request
import de.telekom.usp.proto.msg.Set

fun Get(init: GetRequestBuilder.() -> Unit): Msg {
    val builder = GetRequestBuilder()
    builder.init()
    return builder.message()
}

fun Set(init: SetRequestBuilder.() -> Unit): Msg {
    val builder = SetRequestBuilder()
    builder.init()
    return builder.message()
}

fun Add(init: AddRequestBuilder.() -> Unit): Msg {
    val builder = AddRequestBuilder()
    builder.init()
    return builder.message()
}

fun Delete(init: DeleteRequestBuilder.() -> Unit): Msg {
    val builder = DeleteRequestBuilder()
    builder.init()
    return builder.message()
}

fun Operate(
    command: String,
    commandKey: String,
    sendResponse: Boolean = true,
    init: OperateRequestBuilder.() -> Unit
): Msg {
    val builder = OperateRequestBuilder(command, commandKey, sendResponse)
    builder.init()
    return builder.message()
}

fun GetSupportedDm(init: GetSupportedDmBuilder.() -> Unit): Msg {
    val builder = GetSupportedDmBuilder()
    builder.init()
    return builder.message()
}

fun GetInstances(init: GetInstancesBuilder.() -> Unit): Msg {
    val builder = GetInstancesBuilder()
    builder.init()
    return builder.message()
}

// --- Builder classes -----------------------------------------------------------------------------

abstract class RequestBodyBuilder(type: Header.MsgType) : BodyBuilder(type) {

    fun message(): Msg {
        return Msg(
            header_ = Header(
                msg_type = type,
                msg_id = messageId ?: "$type-${MessageIdFactory.next()}"
            ),
            body = Body(request = request())
        )
    }

    abstract fun request(): Request
}

abstract class AbstractRequestPathBuilder(type: Header.MsgType) : RequestBodyBuilder(type) {

    private val _paths = mutableListOf<String>()
    val paths: List<String>
        get() = _paths

    /**
     * Adds the specified path o this request
     */
    fun path(path: String) = _paths.add(path)
}

class GetRequestBuilder : AbstractRequestPathBuilder(Header.MsgType.GET) {

    var maxDepth = 1

    override fun request() = Request(get_ = Get(param_paths = paths, max_depth = maxDepth))
}

class GetSupportedDmBuilder : AbstractRequestPathBuilder(Header.MsgType.GET_SUPPORTED_DM) {

    var firstLevelOnly = true

    var returnCommands = true

    var returnEvents = true

    var returnParams = true

    override fun request() = Request(
        get_supported_dm = GetSupportedDM(
            obj_paths = paths,
            first_level_only = firstLevelOnly,
            return_commands = returnCommands,
            return_events = returnEvents,
            return_params = returnParams
        )
    )
}

class GetInstancesBuilder : AbstractRequestPathBuilder(Header.MsgType.GET_INSTANCES) {

    var firstLevelOnly = true

    override fun request() =
        Request(get_instances = GetInstances(obj_paths = paths, first_level_only = firstLevelOnly))
}

class SetRequestBuilder : RequestBodyBuilder(Header.MsgType.SET) {

    private var paramSettingsBuilder = mutableListOf<ParamSettingsBuilder>()

    private val updateObjects: List<Set.UpdateObject>
        get() = paramSettingsBuilder.map {
            Set.UpdateObject(obj_path = it.path, param_settings = it.toUpdateParamSettings())
        }

    var allowPartial = true

    fun path(path: String, init: ParamSettingsBuilder.() -> Unit) {
        ParamSettingsBuilder(path).also {
            paramSettingsBuilder.add(it)
            it.init()
        }
    }

    override fun request() = Request(
        set_ = Set(update_objs = updateObjects, allow_partial = allowPartial)
    )
}

class AddRequestBuilder : RequestBodyBuilder(Header.MsgType.ADD) {

    private var paramSettingsBuilder = mutableListOf<ParamSettingsBuilder>()

    private val addObjects: List<Add.CreateObject>
        get() = paramSettingsBuilder.map {
            Add.CreateObject(obj_path = it.path, param_settings = it.toCreateParamSettings())
        }

    var allowPartial = true

    fun path(path: String, init: ParamSettingsBuilder.() -> Unit) {
        ParamSettingsBuilder(path).also {
            paramSettingsBuilder.add(it)
            it.init()
        }
    }

    override fun request() =
        Request(add = Add(create_objs = addObjects, allow_partial = allowPartial))
}

class DeleteRequestBuilder : AbstractRequestPathBuilder(Header.MsgType.DELETE) {

    var allowPartial = true

    override fun request() =
        Request(delete = Delete(obj_paths = paths, allow_partial = allowPartial))
}

class OperateRequestBuilder(val cmd: String, val commandKey: String, val sendResponse: Boolean) :
    RequestBodyBuilder(Header.MsgType.OPERATE) {

    private val args = mutableMapOf<String, String>()

    fun arg(key: String, value: String) {
        args[key] = value
    }

    override fun request() = Request(
        operate = Operate(
            command = cmd,
            command_key = commandKey,
            send_resp = sendResponse,
            input_args = args
        )
    )
}

class ParamSettingsBuilder(val path: String) {
    private val _params = mutableListOf<ParamSettings>()
    val params: List<ParamSettings>
        get() = _params

    fun param(param: String, value: String, isRequired: Boolean = true) {
        _params.add(ParamSettings(param, value, isRequired))
    }
}

private fun ParamSettingsBuilder.toUpdateParamSettings() = params.map {
    Set.UpdateParamSetting(
        param_ = it.param,
        value_ = it.value,
        required = it.isRequired
    )
}

private fun ParamSettingsBuilder.toCreateParamSettings() = params.map {
    Add.CreateParamSetting(
        param_ = it.param,
        value_ = it.value,
        required = it.isRequired
    )
}

data class ParamSettings(val param: String, val value: String, val isRequired: Boolean)

fun main() {
    val get = Get {
        path("asdas")
        maxDepth = 1
    }

    val set = Set {
        allowPartial = false
        path("Device.") {
            param("X", "Y", true)
        }
    }

    val add = Add {
        path("Device.") {
            param("", "")
        }
    }

    val operate = Operate("command", "key") {
        arg("key1", "value2")
        arg("key2", "value3")
    }

    println(get)
    println(set)
    println(add)
    println(operate)
}