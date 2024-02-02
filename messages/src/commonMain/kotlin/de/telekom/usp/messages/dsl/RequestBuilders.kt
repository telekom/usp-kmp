@file:Suppress("FunctionName", "unused", "MemberVisibilityCanBePrivate")

package de.telekom.usp.messages.dsl

import de.telekom.usp.Path
import de.telekom.usp.messages.MessageIdFactory
import de.telekom.usp.proto.msg.Add
import de.telekom.usp.proto.msg.Body
import de.telekom.usp.proto.msg.Delete
import de.telekom.usp.proto.msg.Deregister
import de.telekom.usp.proto.msg.Get
import de.telekom.usp.proto.msg.GetInstances
import de.telekom.usp.proto.msg.GetSupportedDM
import de.telekom.usp.proto.msg.GetSupportedProtocol
import de.telekom.usp.proto.msg.Header
import de.telekom.usp.proto.msg.Msg
import de.telekom.usp.proto.msg.Notify
import de.telekom.usp.proto.msg.Notify.OperationComplete.CommandFailure
import de.telekom.usp.proto.msg.Notify.OperationComplete.OutputArgs
import de.telekom.usp.proto.msg.Operate
import de.telekom.usp.proto.msg.Register
import de.telekom.usp.proto.msg.Request
import de.telekom.usp.proto.msg.Set

fun Get(init: GetRequestBuilder.() -> Unit) = initBuilder(GetRequestBuilder(), init)

fun Set(init: SetRequestBuilder.() -> Unit) = initBuilder(SetRequestBuilder(), init)

fun Add(init: AddRequestBuilder.() -> Unit) = initBuilder(AddRequestBuilder(), init)

fun Delete(init: DeleteRequestBuilder.() -> Unit) = initBuilder(DeleteRequestBuilder(), init)

fun Register(init: RegisterBuilder.() -> Unit) = initBuilder(RegisterBuilder(), init)

fun Deregister(init: DeregisterBuilder.() -> Unit) = initBuilder(DeregisterBuilder(), init)

fun Operate(
    path: String,
    commandKey: String,
    sendResponse: Boolean = true,
    init: OperateRequestBuilder.() -> Unit
) = initBuilder(OperateRequestBuilder(Path(path), commandKey, sendResponse), init)

fun Notify(subscriptionId: String, init: NotifyRequestBuilder.() -> Unit) =
    initBuilder(NotifyRequestBuilder(subscriptionId), init)

fun GetSupportedDm(init: GetSupportedDmBuilder.() -> Unit) =
    initBuilder(GetSupportedDmBuilder(), init)

fun GetSupportedProtocol(
    controllerSupportedProtocolVersions: String,
    init: GetSupportedProtocolBuilder.() -> Unit
) = initBuilder(GetSupportedProtocolBuilder(controllerSupportedProtocolVersions), init)

fun GetInstances(init: GetInstancesBuilder.() -> Unit) = initBuilder(GetInstancesBuilder(), init)

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
    fun path(vararg paths: String) = paths.forEach { _paths.add(Path(it)) }

    /**
     * Adds the specified path o this request
     */
    fun path(vararg paths: Path) = paths.forEach { _paths.add(it) }
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

class NotifyRequestBuilder internal constructor(val subscriptionId: String) :
    RequestMessageBuilder(Header.MsgType.NOTIFY) {

    private var event: EventBuilder? = null

    private var valueChange: Pair<Path, String>? = null

    private var objectCreation: ObjectCreationBuilder? = null

    private var objectDeletion: Path? = null

    private var operationComplete: OperationCompleteBuilder? = null

    private var onBoardRequest: OnBoardRequestBuilder? = null

    var sendResponse = true

    fun event(path: Path, name: String, init: EventBuilder.() -> Unit) {
        event = EventBuilder(path, name).apply {
            init()
        }
    }

    fun event(path: String, name: String, init: EventBuilder.() -> Unit) {
        event = EventBuilder(Path(path), name).apply {
            init()
        }
    }

    fun valueChange(path: Path, value: String) {
        valueChange = path to value
    }

    fun valueChange(path: String, value: String) {
        valueChange = Path(path) to value
    }

    fun objectCreation(path: String, init: ObjectCreationBuilder.() -> Unit) {
        objectCreation = ObjectCreationBuilder(Path(path)).apply {
            init()
        }
    }

    fun objectCreation(path: Path, init: ObjectCreationBuilder.() -> Unit) {
        objectCreation = ObjectCreationBuilder(path).apply {
            init()
        }
    }

    fun objectDeletion(path: Path) {
        objectDeletion = path
    }

    fun objectDeletion(path: String) {
        objectDeletion = Path(path)
    }

    fun operationComplete(
        path: Path,
        commandName: String,
        commandKey: String,
        init: OperationCompleteBuilder.() -> Unit
    ) {
        operationComplete = OperationCompleteBuilder(path, commandName, commandKey).apply {
            init()
        }
    }

    fun operationComplete(
        path: String,
        commandName: String,
        commandKey: String,
        init: OperationCompleteBuilder.() -> Unit
    ) {
        operationComplete = OperationCompleteBuilder(Path(path), commandName, commandKey).apply {
            init()
        }
    }

    fun onBoardRequest(
        oui: String,
        productClass: String,
        serialNumber: String,
        agentSupportedProtocolVersions: String
    ) {
        onBoardRequest =
            OnBoardRequestBuilder(oui, productClass, serialNumber, agentSupportedProtocolVersions)
    }

    override fun buildRequest(): Request {
        return Request(
            notify = Notify(
                subscription_id = subscriptionId,
                send_resp = sendResponse,
                event = event?.event(),
                value_change = valueChange?.let {
                    Notify.ValueChange(
                        it.first.toString(),
                        it.second
                    )
                },
                obj_creation = objectCreation?.objectCreation(),
                obj_deletion = objectDeletion?.let { Notify.ObjectDeletion(it.toString()) },
                oper_complete = operationComplete?.operationComplete(),
                on_board_req = onBoardRequest?.onBoardRequest()
            )
        )
    }
}

class EventBuilder(val path: Path, val name: String) {

    private val _params = mutableMapOf<Path, String>()
    val params: Map<Path, String>
        get() = _params

    fun arg(path: String, value: String) {
        _params[Path(path)] = value
    }

    fun arg(path: Path, value: String) {
        _params[path] = value
    }

    fun event(): Notify.Event {
        return Notify.Event(path.toString(), name, params.mapKeys { it.toString() })
    }
}

class ObjectCreationBuilder(val path: Path) {

    private val _uniqueKeys = mutableMapOf<String, String>()
    val uniqueKeys: Map<String, String>
        get() = _uniqueKeys

    fun uniqueKey(key: String, value: String) {
        _uniqueKeys[key] = value
    }

    fun objectCreation() = Notify.ObjectCreation(path.toString(), uniqueKeys)
}

class OperationCompleteBuilder(val path: Path, val commandName: String, val commandKey: String) {

    private var _outputArgs: MutableMap<String, String>? = null

    var commandFailure: Pair<Int, String>? = null

    fun outputArg(key: String, value: String) {
        if (_outputArgs == null) {
            _outputArgs = mutableMapOf()
        }
        _outputArgs!![key] = value
    }

    fun operationComplete(): Notify.OperationComplete {
        if (_outputArgs != null && commandFailure != null) {
            throw IllegalArgumentException("Must choose one of: output_args or command_failure")
        }
        return Notify.OperationComplete(
            obj_path = path.toString(),
            command_name = commandName,
            command_key = commandKey,
            req_output_args = _outputArgs?.let { OutputArgs(it) },
            cmd_failure = commandFailure?.let { CommandFailure(it.first, it.second) }
        )
    }
}

data class OnBoardRequestBuilder(
    val oui: String,
    val productClass: String,
    val serialNumber: String,
    val agentSupportedProtocolVersions: String

) {
    fun onBoardRequest() =
        Notify.OnBoardRequest(oui, productClass, serialNumber, agentSupportedProtocolVersions)
}

class GetSupportedProtocolBuilder(val controllerSupportedProtocolVersions: String) :
    RequestMessageBuilder(Header.MsgType.GET_SUPPORTED_PROTO) {

    override fun buildRequest() =
        Request(get_supported_protocol = GetSupportedProtocol(controllerSupportedProtocolVersions))
}

class RegisterBuilder : PathRequestBuilder(Header.MsgType.REGISTER) {

    var allowPartial = true

    override fun buildRequest() = Request(
        register = Register(
            allow_partial = allowPartial,
            reg_paths = paths.map { Register.RegistrationPath(it) })
    )
}

class DeregisterBuilder : PathRequestBuilder(Header.MsgType.DEREGISTER) {

    override fun buildRequest() = Request(deregister = Deregister(paths))
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

    val notify = Notify("subscription") {
        operationComplete("Device.", "cmd-name", "my-key") {
            outputArg("arg1", "key1")
        }
    }

    println(get)
    println(set)
    println(add)
    println(operate)
    println(notify)
}