@file:Suppress("FunctionName")

package de.telekom.usp.messages.dsl

import de.telekom.usp.Error
import de.telekom.usp.NoError
import de.telekom.usp.Path
import de.telekom.usp.messages.proto.AddResp
import de.telekom.usp.messages.proto.AddResp.CreatedObjectResult
import de.telekom.usp.messages.proto.Body
import de.telekom.usp.messages.proto.DeleteResp
import de.telekom.usp.messages.proto.DeleteResp.DeletedObjectResult
import de.telekom.usp.messages.proto.DeregisterResp
import de.telekom.usp.messages.proto.DeregisterResp.DeregisteredPathResult
import de.telekom.usp.messages.proto.GetInstancesResp
import de.telekom.usp.messages.proto.GetResp
import de.telekom.usp.messages.proto.GetSupportedDMResp
import de.telekom.usp.messages.proto.GetSupportedProtocolResp
import de.telekom.usp.messages.proto.Header
import de.telekom.usp.messages.proto.Msg
import de.telekom.usp.messages.proto.NotifyResp
import de.telekom.usp.messages.proto.OperateResp
import de.telekom.usp.messages.proto.RegisterResp
import de.telekom.usp.messages.proto.RegisterResp.RegisteredPathResult
import de.telekom.usp.messages.proto.Response
import de.telekom.usp.messages.proto.SetResp
import de.telekom.usp.messages.proto.id
import de.telekom.usp.messages.proto.requireType
import de.telekom.usp.toStrings

fun GetResp(request: Msg, init: GetRespBuilder.() -> Unit) =
    initBuilder(GetRespBuilder(request), init)

fun GetResp(messageId: String, init: GetRespBuilder.() -> Unit) =
    initBuilder(GetRespBuilder(messageId), init)

fun GetSupportedDMResp(request: Msg, init: GetSupportedDMRespBuilder.() -> Unit) =
    initBuilder(GetSupportedDMRespBuilder(request), init)

fun GetSupportedDMResp(messageId: String, init: GetSupportedDMRespBuilder.() -> Unit) =
    initBuilder(GetSupportedDMRespBuilder(messageId), init)

fun GetInstancesResp(request: Msg, init: GetInstancesRespBuilder.() -> Unit) =
    initBuilder(GetInstancesRespBuilder(request), init)

fun GetInstancesResp(messageId: String, init: GetInstancesRespBuilder.() -> Unit) =
    initBuilder(GetInstancesRespBuilder(messageId), init)

fun SetResp(request: Msg, init: SetRespBuilder.() -> Unit) =
    initBuilder(SetRespBuilder(request), init)

fun SetResp(messageId: String, init: SetRespBuilder.() -> Unit) =
    initBuilder(SetRespBuilder(messageId), init)

fun AddResp(request: Msg, init: AddRespBuilder.() -> Unit) =
    initBuilder(AddRespBuilder(request), init)

fun AddResp(messageId: String, init: AddRespBuilder.() -> Unit) =
    initBuilder(AddRespBuilder(messageId), init)

fun DeleteResp(request: Msg, init: DeleteRespBuilder.() -> Unit) =
    initBuilder(DeleteRespBuilder(request), init)

fun DeleteResp(messageId: String, init: DeleteRespBuilder.() -> Unit) =
    initBuilder(DeleteRespBuilder(messageId), init)

fun OperateResp(request: Msg, init: OperateRespBuilder.() -> Unit) =
    initBuilder(OperateRespBuilder(request), init)

fun OperateResp(messageId: String, init: OperateRespBuilder.() -> Unit) =
    initBuilder(OperateRespBuilder(messageId), init)

fun NotifyResp(request: Msg, subscriptionId: String) =
    NotifyRespBuilder(request, subscriptionId).build()

fun NotifyResp(messageId: String, subscriptionId: String) =
    NotifyRespBuilder(messageId, subscriptionId).build()

fun GetSupportedProtocolResp(request: Msg, agentSupportedProtocolVersions: String) =
    GetSupportedProtocolRespBuilder(request, agentSupportedProtocolVersions).build()

fun GetSupportedProtocolResp(messageId: String, agentSupportedProtocolVersions: String) =
    GetSupportedProtocolRespBuilder(messageId, agentSupportedProtocolVersions).build()

fun RegisterResp(request: Msg, init: RegisterRespBuilder.() -> Unit) =
    initBuilder(RegisterRespBuilder(request), init)

fun RegisterResp(messageId: String, init: RegisterRespBuilder.() -> Unit) =
    initBuilder(RegisterRespBuilder(messageId), init)

fun DeregisterResp(request: Msg, init: DeregisterRespBuilder.() -> Unit) =
    initBuilder(DeregisterRespBuilder(request), init)

fun DeregisterResp(messageId: String, init: DeregisterRespBuilder.() -> Unit) =
    initBuilder(DeregisterRespBuilder(messageId), init)


// --- Builder classes -----------------------------------------------------------------------------

abstract class ResponseMessageBuilder internal constructor(
    type: Header.MsgType,
    messageId: String
) : MessageBuilder(type) {

    init {
        this.messageId = messageId
    }

    override fun build(): Msg {
        if (messageId.isNullOrBlank()) {
            throw IllegalArgumentException("Message ID cannot be null or blank for response messages")
        }

        return Msg(
            header_ = Header(
                msg_type = type,
                msg_id = messageId!!
            ),
            body = Body(response = buildResponse())
        )
    }

    abstract fun buildResponse(): Response
}

// --- GetRespBuilder ------------------------------------------------------------------------------

class GetRespBuilder internal constructor(messageId: String) :
    ResponseMessageBuilder(Header.MsgType.GET_RESP, messageId) {

    internal constructor(request: Msg) : this(request.id) {
        request.requireType(Header.MsgType.GET_RESP)
    }

    private val results = mutableListOf<RequestedPathResultBuilder>()

    fun addResult(
        requestedPath: Path,
        error: Error = NoError,
        init: RequestedPathResultBuilder.() -> Unit
    ) {
        addBuilder(RequestedPathResultBuilder(requestedPath, error), results, init)
    }

    fun addResult(
        requestedPath: String,
        error: Error = NoError,
        init: RequestedPathResultBuilder.() -> Unit
    ) {
        addBuilder(RequestedPathResultBuilder(Path(requestedPath), error), results, init)
    }

    override fun buildResponse() = Response(
        get_resp = GetResp(req_path_results = results.map { it.build() })
    )
}

class RequestedPathResultBuilder internal constructor(
    private val requestedPath: Path,
    private val error: Error
) {

    private val resolvedPaths = mutableListOf<ResolvedPathResultBuilder>()

    fun addResolvedPath(resolvedPath: Path, init: ResolvedPathResultBuilder.() -> Unit) {
        addBuilder(ResolvedPathResultBuilder(resolvedPath), resolvedPaths, init)
    }

    fun addResolvedPath(resolvedPath: String, init: ResolvedPathResultBuilder.() -> Unit) {
        addBuilder(ResolvedPathResultBuilder(Path(resolvedPath)), resolvedPaths, init)
    }

    fun build() = GetResp.RequestedPathResult(
        requested_path = requestedPath.toString(),
        err_code = error.code,
        err_msg = if (error.code == 0) "" else error.name,
        resolved_path_results = resolvedPaths.map { it.build() }
    )
}

class ResolvedPathResultBuilder internal constructor(private val resolvedPath: Path) {

    val params = mutableMapOf<String, String>()

    fun build() =
        GetResp.ResolvedPathResult(resolved_path = resolvedPath.toString(), result_params = params)
}

// --- GetSupportedDMRespBuilder -------------------------------------------------------------------

class GetSupportedDMRespBuilder internal constructor(messageId: String) :
    ResponseMessageBuilder(Header.MsgType.GET_SUPPORTED_DM_RESP, messageId) {

    internal constructor(request: Msg) : this(request.id) {
        request.requireType(Header.MsgType.GET_SUPPORTED_DM_RESP)
    }

    private val results = mutableListOf<RequestedObjectResultBuilder>()

    fun addResult(
        requestedPath: Path,
        uri: String,
        error: Error = NoError,
        init: RequestedObjectResultBuilder.() -> Unit
    ) {
        addBuilder(RequestedObjectResultBuilder(requestedPath, uri, error), results, init)
    }

    fun addResult(
        requestedPath: String,
        uri: String,
        error: Error = NoError,
        init: RequestedObjectResultBuilder.() -> Unit
    ) {
        addBuilder(RequestedObjectResultBuilder(Path(requestedPath), uri, error), results, init)
    }

    override fun buildResponse() =
        Response(get_supported_dm_resp = GetSupportedDMResp(results.map { it.build() }))
}

class RequestedObjectResultBuilder internal constructor(
    private val path: Path,
    private val uri: String,
    private val error: Error
) {
    private val objectResults = mutableListOf<SupportedObjectResultBuilder>()

    fun addObject(
        supportedPath: Path,
        accessType: GetSupportedDMResp.ObjAccessType,
        isMultiInstance: Boolean,
        init: SupportedObjectResultBuilder.() -> Unit
    ) {
        addBuilder(
            SupportedObjectResultBuilder(supportedPath, accessType, isMultiInstance),
            objectResults,
            init
        )
    }

    fun addObject(
        supportedPath: String,
        accessType: GetSupportedDMResp.ObjAccessType,
        isMultiInstance: Boolean,
        init: SupportedObjectResultBuilder.() -> Unit
    ) {
        addBuilder(
            SupportedObjectResultBuilder(Path(supportedPath), accessType, isMultiInstance),
            objectResults,
            init
        )
    }

    fun build() = GetSupportedDMResp.RequestedObjectResult(
        req_obj_path = path.toString(),
        err_code = error.code,
        err_msg = if (error.code == 0) "" else error.name,
        data_model_inst_uri = uri,
        supported_objs = objectResults.map { it.build() }
    )
}

class SupportedObjectResultBuilder internal constructor(
    private val path: Path,
    private val accessType: GetSupportedDMResp.ObjAccessType,
    private val isMultiInstance: Boolean
) {
    private val supportedCommands = mutableListOf<SupportedCommandResultBuilder>()
    private val supportedEvents = mutableListOf<SupportedEventResultBuilder>()
    private val supportedParams = mutableListOf<GetSupportedDMResp.SupportedParamResult>()

    val divergentPaths = mutableListOf<String>()

    fun addCommand(
        name: String,
        type: GetSupportedDMResp.CmdType,
        init: SupportedCommandResultBuilder.() -> Unit
    ) {
        addBuilder(SupportedCommandResultBuilder(name, type), supportedCommands, init)
    }

    fun addEvent(name: String, init: SupportedEventResultBuilder.() -> Unit) {
        addBuilder(SupportedEventResultBuilder(name), supportedEvents, init)
    }

    fun addParam(
        name: String, accessType: GetSupportedDMResp.ParamAccessType,
        valueType: GetSupportedDMResp.ParamValueType,
        valueChangeType: GetSupportedDMResp.ValueChangeType
    ) {
        supportedParams.add(
            GetSupportedDMResp.SupportedParamResult(
                param_name = name,
                access = accessType,
                value_type = valueType,
                value_change = valueChangeType
            )
        )
    }

    fun build(): GetSupportedDMResp.SupportedObjectResult {
        return GetSupportedDMResp.SupportedObjectResult(
            supported_obj_path = path.toString(),
            access = accessType,
            is_multi_instance = isMultiInstance,
            supported_commands = supportedCommands.map { it.build() },
            supported_events = supportedEvents.map { it.build() },
            supported_params = supportedParams,
            divergent_paths = divergentPaths
        )
    }
}

class SupportedCommandResultBuilder internal constructor(
    private val name: String,
    private val type: GetSupportedDMResp.CmdType
) {
    val inputArgs = mutableListOf<String>()

    val outputArgs = mutableListOf<String>()

    fun build() = GetSupportedDMResp.SupportedCommandResult(
        command_name = name,
        input_arg_names = inputArgs,
        output_arg_names = outputArgs,
        command_type = type
    )
}

class SupportedEventResultBuilder internal constructor(private val name: String) {

    val args = mutableListOf<String>()

    fun build() = GetSupportedDMResp.SupportedEventResult(event_name = name, arg_names = args)
}

// --- GetInstancesRespBuilder ---------------------------------------------------------------------

class GetInstancesRespBuilder internal constructor(messageId: String) :
    ResponseMessageBuilder(Header.MsgType.GET_INSTANCES_RESP, messageId) {

    internal constructor(request: Msg) : this(request.id) {
        request.requireType(Header.MsgType.GET_SUPPORTED_DM_RESP)
    }

    private val results = mutableListOf<GetInstancesRespResultBuilder>()

    fun addResult(
        path: Path,
        error: Error = NoError,
        init: GetInstancesRespResultBuilder.() -> Unit
    ) {
        addBuilder(GetInstancesRespResultBuilder(path, error), results, init)
    }

    fun addResult(
        path: String,
        error: Error = NoError,
        init: GetInstancesRespResultBuilder.() -> Unit
    ) {
        addBuilder(GetInstancesRespResultBuilder(Path(path), error), results, init)
    }

    override fun buildResponse() =
        Response(get_instances_resp = GetInstancesResp(req_path_results = results.map { it.build() }))
}

class GetInstancesRespResultBuilder(private val path: Path, private val error: Error) {

    private val currInstances = mutableListOf<CurrInstanceBuilder>()

    fun addCurrInstance(instantiatedPath: Path, init: CurrInstanceBuilder.() -> Unit) {
        addBuilder(CurrInstanceBuilder(instantiatedPath), currInstances, init)
    }

    fun addCurrInstance(instantiatedPath: String, init: CurrInstanceBuilder.() -> Unit) {
        addBuilder(CurrInstanceBuilder(Path(instantiatedPath)), currInstances, init)
    }

    fun build() = GetInstancesResp.RequestedPathResult(
        requested_path = path.toString(),
        err_code = error.code,
        err_msg = if (error.code == 0) "" else error.name,
        curr_insts = currInstances.map { it.build() }
    )
}

class CurrInstanceBuilder(private val path: Path) {

    val uniqueKeys = mutableMapOf<String, String>()

    fun build() = GetInstancesResp.CurrInstance(path.toString(), uniqueKeys)
}

// --- SetRespBuilder ------------------------------------------------------------------------------

class SetRespBuilder internal constructor(messageId: String) :
    ResponseMessageBuilder(Header.MsgType.SET_RESP, messageId) {

    internal constructor(request: Msg) : this(request.id) {
        request.requireType(Header.MsgType.SET_RESP)
    }

    private val results = mutableListOf<SetOperationStatusBuilder>()

    fun addResult(requestedPath: Path, init: SetOperationStatusBuilder.() -> Unit) {
        addBuilder(SetOperationStatusBuilder(requestedPath), results, init)
    }

    fun addResult(requestedPath: String, init: SetOperationStatusBuilder.() -> Unit) {
        addBuilder(SetOperationStatusBuilder(Path(requestedPath)), results, init)
    }

    override fun buildResponse() =
        Response(set_resp = SetResp(updated_obj_results = results.map { it.build() }))
}

class SetOperationStatusBuilder internal constructor(private val path: Path) {

    private var failure: SetOperationFailureBuilder? = null

    private var succes: SetOperationSuccessBuilder? = null

    fun failure(error: Error, init: SetOperationFailureBuilder.() -> Unit) {
        failure = SetOperationFailureBuilder(error).also(init)
    }

    fun success(init: SetOperationSuccessBuilder.() -> Unit) {
        succes = SetOperationSuccessBuilder().also(init)
    }

    fun build() = SetResp.UpdatedObjectResult(
        requested_path = path.toString(),
        oper_status = buildStatus()
    )

    private fun buildStatus(): SetResp.UpdatedObjectResult.OperationStatus {
        return if (failure != null) {
            SetResp.UpdatedObjectResult.OperationStatus(oper_failure = failure!!.build())
        } else if (succes != null) {
            SetResp.UpdatedObjectResult.OperationStatus(oper_success = succes!!.build())
        } else {
            throw IllegalStateException("SetOperationStatus must contain either failure or success")
        }
    }
}

class SetOperationFailureBuilder internal constructor(private val error: Error) {

    private val failures = mutableListOf<UpdatedInstanceFailureBuilder>()

    fun addInstance(path: Path, init: UpdatedInstanceFailureBuilder.() -> Unit) {
        addBuilder(UpdatedInstanceFailureBuilder(path), failures, init)
    }

    fun addInstance(path: String, init: UpdatedInstanceFailureBuilder.() -> Unit) {
        addBuilder(UpdatedInstanceFailureBuilder(Path(path)), failures, init)
    }

    fun build(): SetResp.UpdatedObjectResult.OperationStatus.OperationFailure {
        return SetResp.UpdatedObjectResult.OperationStatus.OperationFailure(
            err_code = error.code,
            err_msg = error.name,
            updated_inst_failures = failures.map { it.build() }
        )
    }
}

class UpdatedInstanceFailureBuilder internal constructor(private val path: Path) {

    private val errors = mutableListOf<ParameterizedError>()

    fun addError(param: String, error: Error) {
        errors.add(ParameterizedError(param, error))
    }

    fun build() = SetResp.UpdatedInstanceFailure(path.toString(), errors.toSetParameters())
}

class SetOperationSuccessBuilder internal constructor() {

    private val result = mutableListOf<UpdatedInstanceResultBuilder>()

    fun addInstance(affectedPath: Path, init: UpdatedInstanceResultBuilder.() -> Unit) {
        addBuilder(UpdatedInstanceResultBuilder(affectedPath), result, init)
    }

    fun addInstance(affectedPath: String, init: UpdatedInstanceResultBuilder.() -> Unit) {
        addBuilder(UpdatedInstanceResultBuilder(Path(affectedPath)), result, init)
    }

    fun build() =
        SetResp.UpdatedObjectResult.OperationStatus.OperationSuccess(result.map { it.build() })
}

class UpdatedInstanceResultBuilder internal constructor(private val path: Path) {

    private val errors = mutableListOf<ParameterizedError>()

    fun addError(param: String, error: Error) {
        errors.add(ParameterizedError(param, error))
    }

    val params = mutableMapOf<String, String>()

    fun build(): SetResp.UpdatedInstanceResult {
        return SetResp.UpdatedInstanceResult(
            affected_path = path.toString(),
            param_errs = errors.toSetParameters(),
            updated_params = params
        )
    }
}

// --- AddRespBuilder ------------------------------------------------------------------------------

class AddRespBuilder internal constructor(messageId: String) :
    ResponseMessageBuilder(Header.MsgType.ADD_RESP, messageId) {

    internal constructor(request: Msg) : this(request.id) {
        request.requireType(Header.MsgType.ADD_RESP)
    }

    private val results = mutableListOf<CreatedObjectResultBuilder>()

    fun addResult(requestedPath: Path, init: CreatedObjectResultBuilder.() -> Unit) {
        addBuilder(CreatedObjectResultBuilder(requestedPath), results, init)
    }

    fun addResult(requestedPath: String, init: CreatedObjectResultBuilder.() -> Unit) {
        addBuilder(CreatedObjectResultBuilder(Path(requestedPath)), results, init)
    }

    override fun buildResponse() = Response(add_resp = AddResp(results.map { it.build() }))
}

class CreatedObjectResultBuilder internal constructor(private val path: Path) {

    private var failure: Error? = null

    private var success: AddOperationSuccessBuilder? = null

    fun failure(error: Error) {
        this.failure = error
    }

    fun success(instantiatedPath: Path, init: AddOperationSuccessBuilder.() -> Unit) {
        success = AddOperationSuccessBuilder(instantiatedPath).also(init)
    }

    fun success(instantiatedPath: String, init: AddOperationSuccessBuilder.() -> Unit) {
        success = AddOperationSuccessBuilder(Path(instantiatedPath)).also(init)
    }

    fun build(): CreatedObjectResult {
        return if (failure != null) {
            CreatedObjectResult(
                requested_path = path.toString(),
                oper_status = CreatedObjectResult.OperationStatus(
                    oper_failure = CreatedObjectResult.OperationStatus.OperationFailure(
                        failure!!.code,
                        failure!!.name
                    )
                )
            )
        } else if (success != null) {
            CreatedObjectResult(
                requested_path = path.toString(),
                oper_status = CreatedObjectResult.OperationStatus(oper_success = success!!.build())
            )
        } else {
            throw IllegalStateException("Either success or failure must exist")
        }
    }
}

class AddOperationSuccessBuilder internal constructor(private val instantiatedPath: Path) {

    private val errors = mutableListOf<ParameterizedError>()

    val uniqueKeys = mutableMapOf<String, String>()

    fun addError(param: String, error: Error) {
        errors.add(ParameterizedError(param, error))
    }

    fun build() = CreatedObjectResult.OperationStatus.OperationSuccess(
        instantiated_path = instantiatedPath.toString(),
        param_errs = errors.toAddParameters(),
        unique_keys = uniqueKeys
    )
}

// --- DeleteRespBuilder ---------------------------------------------------------------------------

class DeleteRespBuilder internal constructor(messageId: String) :
    ResponseMessageBuilder(Header.MsgType.DELETE_RESP, messageId) {

    internal constructor(request: Msg) : this(request.id) {
        request.requireType(Header.MsgType.DELETE_RESP)
    }

    private val results = mutableListOf<DeletedObjectResultBuilder>()

    fun addResult(requestedPath: Path, init: DeletedObjectResultBuilder.() -> Unit) {
        addBuilder(DeletedObjectResultBuilder(requestedPath), results, init)
    }

    fun addResult(requestedPath: String, init: DeletedObjectResultBuilder.() -> Unit) {
        addBuilder(DeletedObjectResultBuilder(Path(requestedPath)), results, init)
    }

    override fun buildResponse() = Response(delete_resp = DeleteResp(results.map { it.build() }))
}

class DeletedObjectResultBuilder internal constructor(private val path: Path) {

    private var failure: Error? = null

    private var success: DeleteOperationSuccessBuilder? = null

    fun failure(error: Error) {
        this.failure = error
    }

    fun success(affectedPath: Path, init: DeleteOperationSuccessBuilder.() -> Unit) {
        success = DeleteOperationSuccessBuilder(affectedPath).also(init)
    }

    fun success(affectedPath: String, init: DeleteOperationSuccessBuilder.() -> Unit) {
        success = DeleteOperationSuccessBuilder(Path(affectedPath)).also(init)
    }

    fun build(): DeletedObjectResult {
        return if (failure != null) {
            DeletedObjectResult(
                requested_path = path.toString(),
                oper_status = DeletedObjectResult.OperationStatus(
                    oper_failure = DeletedObjectResult.OperationStatus.OperationFailure(
                        failure!!.code,
                        failure!!.name
                    )
                )
            )
        } else if (success != null) {
            DeletedObjectResult(
                requested_path = path.toString(),
                oper_status = DeletedObjectResult.OperationStatus(oper_success = success!!.build())
            )
        } else {
            throw IllegalStateException("Either success or failure must be set")
        }
    }
}

class DeleteOperationSuccessBuilder internal constructor(private val path: Path) {

    private val errors = mutableListOf<ParameterizedError>()

    private val paths = mutableListOf<Path>()

    fun addPath(affectedPath: Path) {
        paths.add(affectedPath)
    }

    fun addPath(affectedPath: String) {
        paths.add(Path(affectedPath))
    }

    fun addError(unaffectedPath: String, error: Error) {
        errors.add(ParameterizedError(unaffectedPath, error))
    }

    fun addError(unaffectedPath: Path, error: Error) {
        errors.add(ParameterizedError(unaffectedPath.toString(), error))
    }

    fun build(): DeletedObjectResult.OperationStatus.OperationSuccess {
        return DeletedObjectResult.OperationStatus.OperationSuccess(
            affected_paths = paths.toStrings(),
            unaffected_path_errs = errors.toDeleteParameters()
        )
    }
}

// --- OperateRespBuilder --------------------------------------------------------------------------

class OperateRespBuilder internal constructor(messageId: String) :
    ResponseMessageBuilder(Header.MsgType.OPERATE_RESP, messageId) {

    internal constructor(request: Msg) : this(request.id) {
        request.requireType(Header.MsgType.OPERATE_RESP)
    }

    private val results = mutableListOf<OperationResultBuilder>()

    fun addResult(executedCommand: Path, init: OperationResultBuilder.() -> Unit) {
        addBuilder(OperationResultBuilder(executedCommand), results, init)
    }

    fun addResult(executedCommand: String, init: OperationResultBuilder.() -> Unit) {
        addBuilder(OperationResultBuilder(Path(executedCommand)), results, init)
    }

    override fun buildResponse() = Response(operate_resp = OperateResp(results.map { it.build() }))
}

class OperationResultBuilder internal constructor(private val path: Path) {

    private var requestObjectPath: Path? = null
    private var commandFailure: Error? = null

    var requestedOutputArgs = mutableMapOf<String, String>()

    fun requestObjectPath(path: Path) {
        this.requestObjectPath = path
    }

    fun requestObjectPath(path: String) {
        this.requestObjectPath = Path(path)
    }

    fun commandFailure(error: Error) {
        this.commandFailure = error
    }

    fun build(): OperateResp.OperationResult {
        return if (requestObjectPath != null) {
            OperateResp.OperationResult(
                executed_command = path.toString(),
                req_obj_path = requestObjectPath!!.toString()
            )
        } else if (requestedOutputArgs.isNotEmpty()) {
            OperateResp.OperationResult(
                executed_command = path.toString(),
                req_output_args = OperateResp.OperationResult.OutputArgs(requestedOutputArgs)
            )
        } else if (commandFailure != null) {
            OperateResp.OperationResult(
                executed_command = path.toString(),
                cmd_failure = OperateResp.OperationResult.CommandFailure(
                    commandFailure!!.code,
                    commandFailure!!.name
                )
            )
        } else {
            throw IllegalStateException()
        }
    }
}

// --- NotifyRespBuilder ---------------------------------------------------------------------------

class NotifyRespBuilder internal constructor(
    messageId: String,
    private val subscriptionId: String
) :
    ResponseMessageBuilder(Header.MsgType.NOTIFY_RESP, messageId) {

    internal constructor(request: Msg, subscriptionId: String) : this(request.id, subscriptionId) {
        request.requireType(Header.MsgType.NOTIFY_RESP)
    }

    override fun buildResponse() = Response(notify_resp = NotifyResp(subscriptionId))
}

// --- GetSupportedProtocolRespBuilder -------------------------------------------------------------

class GetSupportedProtocolRespBuilder internal constructor(
    messageId: String,
    private val agentSupportedProtocolVersions: String
) :
    ResponseMessageBuilder(Header.MsgType.GET_SUPPORTED_PROTO_RESP, messageId) {

    internal constructor(request: Msg, agentSupportedProtocolVersions: String) : this(
        request.id,
        agentSupportedProtocolVersions
    ) {
        request.requireType(Header.MsgType.GET_SUPPORTED_PROTO_RESP)
    }

    override fun buildResponse() = Response(
        get_supported_protocol_resp = GetSupportedProtocolResp(agentSupportedProtocolVersions)
    )
}

// --- RegisterRespBuilder -------------------------------------------------------------------------

class RegisterRespBuilder internal constructor(messageId: String) :
    ResponseMessageBuilder(Header.MsgType.REGISTER_RESP, messageId) {

    internal constructor(request: Msg) : this(request.id) {
        request.requireType(Header.MsgType.REGISTER_RESP)
    }

    private val results = mutableListOf<RegisteredPathResultBuilder>()

    fun addResult(requestedPath: Path, init: RegisteredPathResultBuilder.() -> Unit) {
        addBuilder(RegisteredPathResultBuilder(requestedPath), results, init)
    }

    fun addResult(requestedPath: String, init: RegisteredPathResultBuilder.() -> Unit) {
        addBuilder(RegisteredPathResultBuilder(Path(requestedPath)), results, init)
    }

    override fun buildResponse() =
        Response(register_resp = RegisterResp(results.map { it.build() }))
}

class RegisteredPathResultBuilder internal constructor(private val path: Path) {

    private var registeredPath: Path? = null
    private var failure: Error? = null

    fun registeredPath(path: Path) {
        this.registeredPath = path
    }

    fun registeredPath(path: String) {
        registeredPath(Path(path))
    }

    fun failure(error: Error) {
        this.failure = error
    }

    fun build(): RegisteredPathResult {
        return if (registeredPath != null) {
            RegisteredPathResult(path.toString(), buildSuccessStatus())
        } else if (failure != null) {
            RegisteredPathResult(path.toString(), buildFailureStatus())
        } else {
            throw IllegalStateException("Either registered_path or failure must be provided")
        }
    }

    private fun buildSuccessStatus() = RegisteredPathResult.OperationStatus(
        oper_success = RegisteredPathResult.OperationStatus.OperationSuccess(
            registeredPath.toString()
        )
    )

    private fun buildFailureStatus() = RegisteredPathResult.OperationStatus(
        oper_failure = RegisteredPathResult.OperationStatus.OperationFailure(
            err_code = failure!!.code,
            err_msg = failure!!.name
        )
    )
}

// --- DeregisterRespBuilder -----------------------------------------------------------------------

class DeregisterRespBuilder internal constructor(messageId: String) :
    ResponseMessageBuilder(Header.MsgType.DEREGISTER_RESP, messageId) {

    internal constructor(request: Msg) : this(request.id) {
        request.requireType(Header.MsgType.DEREGISTER_RESP)
    }

    private val results = mutableListOf<DeregisteredPathResultBuilder>()

    fun addResult(requestedPath: Path, init: DeregisteredPathResultBuilder.() -> Unit) {
        addBuilder(DeregisteredPathResultBuilder(requestedPath), results, init)
    }

    fun addResult(requestedPath: String, init: DeregisteredPathResultBuilder.() -> Unit) {
        addBuilder(DeregisteredPathResultBuilder(Path(requestedPath)), results, init)
    }

    override fun buildResponse() =
        Response(deregister_resp = DeregisterResp(results.map { it.build() }))
}

class DeregisteredPathResultBuilder internal constructor(private val path: Path) {

    private val deregisteredPaths = mutableListOf<Path>()
    private var failure: Error? = null

    fun addDeregisteredPath(path: Path) {
        deregisteredPaths.add(path)
    }

    fun addDeregisteredPath(path: String) {
        addDeregisteredPath(Path(path))
    }

    fun failure(error: Error) {
        this.failure = error
    }

    fun build(): DeregisteredPathResult {
        return if (deregisteredPaths.isNotEmpty()) {
            DeregisteredPathResult(path.toString(), buildSuccessStatus())
        } else if (failure != null) {
            DeregisteredPathResult(path.toString(), buildFailureStatus())
        } else {
            throw IllegalStateException("Either registered_paths or failure must be provided")
        }
    }

    private fun buildSuccessStatus() = DeregisteredPathResult.OperationStatus(
        oper_success = DeregisteredPathResult.OperationStatus.OperationSuccess(
            deregisteredPaths.toStrings()
        )
    )

    private fun buildFailureStatus() = DeregisteredPathResult.OperationStatus(
        oper_failure = DeregisteredPathResult.OperationStatus.OperationFailure(
            err_code = failure!!.code,
            err_msg = failure!!.name
        )
    )
}

// --- ParameterizedError --------------------------------------------------------------------------

internal data class ParameterizedError(val param: String, val error: Error)

private fun List<ParameterizedError>.toSetParameters() = map {
    SetResp.ParameterError(param_ = it.param, err_code = it.error.code, err_msg = it.error.name)
}

private fun List<ParameterizedError>.toAddParameters() = map {
    AddResp.ParameterError(param_ = it.param, err_code = it.error.code, err_msg = it.error.name)
}

private fun List<ParameterizedError>.toDeleteParameters() = map {
    DeleteResp.UnaffectedPathError(it.param, it.error.code, it.error.name)
}