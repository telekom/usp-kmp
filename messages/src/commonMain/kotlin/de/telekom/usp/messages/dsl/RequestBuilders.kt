@file:Suppress("FunctionName")

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

/**
 * Create a new `Msg` of type GET. Sample usage:
 *
 * ```kotlin
 * val msg = Get {
 *     maxDepth = 2
 *     path("Device.")
 * }
 * ```
 */
fun Get(init: GetRequestBuilder.() -> Unit) = initBuilder(GetRequestBuilder(), init)

/**
 * Create a new `Msg` of type SET. Sample usage:
 *
 * ```kotlin
 * val msg = Set {
 *     allowPartial = false
 *     path("Device.") {
 *         params["param1"] = "value1" required true
 *     }
 * }
 * ```
 */
fun Set(init: SetRequestBuilder.() -> Unit) = initBuilder(SetRequestBuilder(), init)

/**
 * Create a new `Msg` of type ADD. Sample usage:
 *
 * ```kotlin
 * val msg = Add {
 *     allowPartial = false
 *     path("Device.") {
 *         params["param1"] = "value1" required true
 *     }
 * }
 * ```
 */
fun Add(init: AddRequestBuilder.() -> Unit) = initBuilder(AddRequestBuilder(), init)

/**
 * Create a new `Msg` of type DELETE. Sample usage:
 *
 * ```kotlin
 * val msg = Delete {
 *     allowPartial = false
 *     path("Device.")
 * }
 * ```
 */
fun Delete(init: DeleteRequestBuilder.() -> Unit) = initBuilder(DeleteRequestBuilder(), init)

/**
 * Create a new `Msg` of type REGISTER. Sample usage:
 *
 * ```kotlin
 * val msg = Register {
 *     allowPartial = false
 *     path("Device.")
 * }
 * ```
 */
fun Register(init: RegisterBuilder.() -> Unit) = initBuilder(RegisterBuilder(), init)

/**
 * Create a new `Msg` of type DEREGISTER. Sample usage:
 *
 * ```kotlin
 * val msg = Deregister {
 *     path("Device.")
 * }
 * ```
 */
fun Deregister(init: DeregisterBuilder.() -> Unit) = initBuilder(DeregisterBuilder(), init)

/**
 * Create a new `Msg` of type OPERATE. Sample usage:
 *
 * ```kotlin
 * val msg = Operate("Device.Reboot()", "cmd_key") {
 *     args["arg1"] = "value1"
 * }
 * ```
 */
fun Operate(
    path: String,
    commandKey: String,
    sendResponse: Boolean = true,
    init: OperateRequestBuilder.() -> Unit
) = initBuilder(OperateRequestBuilder(Path(path), commandKey, sendResponse), init)

/**
 * Create a new `Msg` of type OPERATE. Sample usage:
 *
 * ```kotlin
 * val msg = Operate(Reboot, "cmd_key") { }
 * ```
 */
fun Operate(
    path: Path,
    commandKey: String,
    sendResponse: Boolean = true,
    init: OperateRequestBuilder.() -> Unit
) = initBuilder(OperateRequestBuilder(path, commandKey, sendResponse), init)

/**
 * Create a new `Msg` of type NOTIFY. Sample usage:
 *
 * ```kotlin
 * val msg = Notify("subscription_1") {
 *     event("Device.SampleEvent!", "event_name_1") {
 *         params["param1"] = "value1"
 *     }
 * }
 * ```
 */
fun Notify(subscriptionId: String, init: NotifyRequestBuilder.() -> Unit) =
    initBuilder(NotifyRequestBuilder(subscriptionId), init)

/**
 * Create a new `Msg` of type GET_SUPPORTED_DM. Sample usage:
 *
 * ```kotlin
 * val msg = GetSupportedDm {
 *     firstLevelOnly = false
 *     returnCommands = true
 *     returnEvents = false
 *     returnParams = true
 * }
 * ```
 */
fun GetSupportedDm(init: GetSupportedDmBuilder.() -> Unit) =
    initBuilder(GetSupportedDmBuilder(), init)

/**
 * Create a new `Msg` of type GET_SUPPORTED_PROTO. Sample usage:
 *
 * ```kotlin
 * val msg = GetSupportedProtocol("1.2,1.3")
 * ```
 */
fun GetSupportedProtocol(
    controllerSupportedProtocolVersions: String
) = initBuilder(GetSupportedProtocolBuilder(controllerSupportedProtocolVersions)) { }

/**
 * Create a new `Msg` of type GET_INSTANCES. Sample usage:
 *
 * ```kotlin
 * val msg = GetInstances {
 *     firstLevelOnly = false
 *     path("Device.")
 * }
 * ```
 */
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
    private val path: Path,
    private val commandKey: String,
    private val sendResponse: Boolean
) :
    RequestMessageBuilder(Header.MsgType.OPERATE) {

    init {
        require(path.isCommand()) { "Operate request must contain a command path: '$path'" }
    }

    val args = mutableMapOf<String, String>()

    override fun buildRequest() = Request(
        operate = Operate(
            command = path.toString(),
            command_key = commandKey,
            send_resp = sendResponse,
            input_args = args
        )
    )
}

class NotifyRequestBuilder internal constructor(private val subscriptionId: String) :
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

class EventBuilder internal constructor(private val path: Path, private val name: String) {

    init {
        require(path.isEvent()) { "Notify event request must contain an event path: '$path'" }
    }

    val params = mutableMapOf<String, String>()

    fun event(): Notify.Event {
        return Notify.Event(path.toString(), name, params)
    }
}

class ObjectCreationBuilder internal constructor(private val path: Path) {

    val uniqueKeys = mutableMapOf<String, String>()

    fun objectCreation() = Notify.ObjectCreation(path.toString(), uniqueKeys)
}

class OperationCompleteBuilder internal constructor(
    private val path: Path,
    private val commandName: String,
    private val commandKey: String
) {

    val outputArgs = mutableMapOf<String, String>()

    var commandFailure: Pair<Int, String>? = null

    fun operationComplete(): Notify.OperationComplete {
        if (outputArgs.isNotEmpty() && commandFailure != null) {
            throw IllegalArgumentException("Must choose one of: output_args or command_failure")
        }
        return Notify.OperationComplete(
            obj_path = path.toString(),
            command_name = commandName,
            command_key = commandKey,
            req_output_args = if (outputArgs.isNotEmpty()) OutputArgs(outputArgs) else null,
            cmd_failure = commandFailure?.let { CommandFailure(it.first, it.second) }
        )
    }
}

data class OnBoardRequestBuilder internal constructor(
    val oui: String,
    val productClass: String,
    val serialNumber: String,
    val agentSupportedProtocolVersions: String
) {

    fun onBoardRequest() =
        Notify.OnBoardRequest(oui, productClass, serialNumber, agentSupportedProtocolVersions)
}

class GetSupportedProtocolBuilder internal constructor(private val controllerSupportedProtocolVersions: String) :
    RequestMessageBuilder(Header.MsgType.GET_SUPPORTED_PROTO) {

    override fun buildRequest() =
        Request(get_supported_protocol = GetSupportedProtocol(controllerSupportedProtocolVersions))
}

class RegisterBuilder internal constructor() : PathRequestBuilder(Header.MsgType.REGISTER) {

    var allowPartial = true

    override fun buildRequest() = Request(
        register = Register(
            allow_partial = allowPartial,
            reg_paths = paths.map { Register.RegistrationPath(it) })
    )
}

class DeregisterBuilder internal constructor() : PathRequestBuilder(Header.MsgType.DEREGISTER) {

    override fun buildRequest() = Request(deregister = Deregister(paths))
}


class ParamSettingsBuilder internal constructor(val path: String) {

    val params = mutableMapOf<String, ParamValue>()
}

data class ParamValue internal constructor(val value: String, val isRequired: Boolean)

infix fun String.required(isRequired: Boolean): ParamValue = ParamValue(this, isRequired)

private fun ParamSettingsBuilder.toUpdateParamSettings() = params.map {
    Set.UpdateParamSetting(
        param_ = it.key,
        value_ = it.value.value,
        required = it.value.isRequired
    )
}

private fun ParamSettingsBuilder.toCreateParamSettings() = params.map {
    Add.CreateParamSetting(
        param_ = it.key,
        value_ = it.value.value,
        required = it.value.isRequired
    )
}