@file:Suppress("FunctionName", "unused", "MemberVisibilityCanBePrivate")

package de.telekom.usp.messages.dsl

import de.telekom.usp.Path
import de.telekom.usp.messages.MessageIdFactory
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

fun Get(init: GetRequestBuilder.() -> Unit) = GetRequestBuilder().run {
    init()
    build()
}

fun Set(init: SetRequestBuilder.() -> Unit) = SetRequestBuilder().run {
    init()
    build()
}

fun Add(init: AddRequestBuilder.() -> Unit) = AddRequestBuilder().run {
    init()
    build()
}

fun Delete(init: DeleteRequestBuilder.() -> Unit) = DeleteRequestBuilder().run {
    init()
    build()
}

fun Operate(
    path: String,
    commandKey: String,
    sendResponse: Boolean = true,
    init: OperateRequestBuilder.() -> Unit
) = OperateRequestBuilder(Path(path), commandKey, sendResponse).run {
    init()
    build()
}

fun GetSupportedDm(init: GetSupportedDmBuilder.() -> Unit) = GetSupportedDmBuilder().run {
    init()
    build()
}

fun GetInstances(init: GetInstancesBuilder.() -> Unit) = GetInstancesBuilder().run {
    init()
    build()
}

// --- Builder classes -----------------------------------------------------------------------------

abstract class RequestMessageBuilder internal constructor(type: Header.MsgType) :
    MessageBuilder(type) {

    override fun build(): Msg {
        return Msg(
            header_ = Header(
                msg_type = type,
                msg_id = messageId ?: "$type-${MessageIdFactory.next()}"
            ),
            body = Body(request = buildRequest())
        )
    }

    abstract fun buildRequest(): Request
}

abstract class PathRequestBuilder internal constructor(type: Header.MsgType) :
    RequestMessageBuilder(type) {

    private val _paths = mutableListOf<Path>()
    val paths: List<String>
        get() = _paths.map { it.toString() }

    /**
     * Adds the specified path o this request
     */
    fun path(path: String) = _paths.add(Path(path))

    /**
     * Adds the specified path o this request
     */
    fun path(path: Path) = _paths.add(path)
}

class GetRequestBuilder internal constructor() : PathRequestBuilder(Header.MsgType.GET) {

    var maxDepth = 1

    override fun buildRequest() = Request(get_ = Get(param_paths = paths, max_depth = maxDepth))
}

class GetSupportedDmBuilder internal constructor() :
    PathRequestBuilder(Header.MsgType.GET_SUPPORTED_DM) {

    var firstLevelOnly = true

    var returnCommands = true

    var returnEvents = true

    var returnParams = true

    override fun buildRequest() = Request(
        get_supported_dm = GetSupportedDM(
            obj_paths = paths,
            first_level_only = firstLevelOnly,
            return_commands = returnCommands,
            return_events = returnEvents,
            return_params = returnParams
        )
    )
}

class GetInstancesBuilder internal constructor() :
    PathRequestBuilder(Header.MsgType.GET_INSTANCES) {

    var firstLevelOnly = true

    override fun buildRequest() =
        Request(get_instances = GetInstances(obj_paths = paths, first_level_only = firstLevelOnly))
}

class SetRequestBuilder internal constructor() : RequestMessageBuilder(Header.MsgType.SET) {

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

    override fun buildRequest() = Request(
        set_ = Set(update_objs = updateObjects, allow_partial = allowPartial)
    )
}

class AddRequestBuilder internal constructor() : RequestMessageBuilder(Header.MsgType.ADD) {

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

    override fun buildRequest() =
        Request(add = Add(create_objs = addObjects, allow_partial = allowPartial))
}

class DeleteRequestBuilder internal constructor() : PathRequestBuilder(Header.MsgType.DELETE) {

    var allowPartial = true

    override fun buildRequest() =
        Request(delete = Delete(obj_paths = paths, allow_partial = allowPartial))
}

class OperateRequestBuilder internal constructor(
    val path: Path,
    val commandKey: String,
    val sendResponse: Boolean
) :
    RequestMessageBuilder(Header.MsgType.OPERATE) {

    init {
        require(path.isCommand()) { "Operate request doesn't contain a command: '$path'" }
    }

    private val args = mutableMapOf<String, String>()

    fun arg(key: String, value: String) {
        args[key] = value
    }

    override fun buildRequest() = Request(
        operate = Operate(
            command = path.toString(),
            command_key = commandKey,
            send_resp = sendResponse,
            input_args = args
        )
    )
}

class ParamSettingsBuilder internal constructor(val path: String) {
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

data class ParamSettings internal constructor(
    val param: String,
    val value: String,
    val isRequired: Boolean
)

fun main() {
    val get = Get {
        path("Device.DeviceInfo.")
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

    val operate = Operate("Device.Command()", "key") {
        arg("key1", "value2")
        arg("key2", "value3")
    }

    println(get)
    println(set)
    println(add)
    println(operate)
}