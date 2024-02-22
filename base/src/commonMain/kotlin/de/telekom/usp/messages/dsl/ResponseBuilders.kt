@file:Suppress("FunctionName")

package de.telekom.usp.messages.dsl

import de.telekom.usp.Error
import de.telekom.usp.NoError
import de.telekom.usp.Path
import de.telekom.usp.messages.proto.Body
import de.telekom.usp.messages.proto.GetResp
import de.telekom.usp.messages.proto.Header
import de.telekom.usp.messages.proto.Msg
import de.telekom.usp.messages.proto.Response
import de.telekom.usp.messages.proto.id
import de.telekom.usp.messages.proto.requireType

fun GetResp(request: Msg, init: GetRespBuilder.() -> Unit) =
    initBuilder(GetRespBuilder(request), init)

fun GetResp(messageId: String, init: GetRespBuilder.() -> Unit) =
    initBuilder(GetRespBuilder(messageId), init)

// --- Builder classes -----------------------------------------------------------------------------

abstract class ResponseMessageBuilder internal constructor(
    type: Header.MsgType,
    messageId: String
) : MessageBuilder(type) {

    init {
        this.messageId = messageId
    }

    override fun build(): Msg {
        val msgId = messageId
        if (msgId.isNullOrBlank()) {
            throw IllegalArgumentException("Message ID cannot be null or blank for response messages")
        }

        return Msg(
            header_ = Header(
                msg_type = type,
                msg_id = msgId
            ),
            body = Body(response = buildResponse())
        )
    }

    abstract fun buildResponse(): Response
}

class GetRespBuilder internal constructor(messageId: String) :
    ResponseMessageBuilder(Header.MsgType.GET_RESP, messageId) {

    internal constructor(request: Msg) : this(request.id) {
        request.requireType(Header.MsgType.GET_RESP)
    }

    private val results = mutableListOf<RequestedPathResultBuilder>()

    fun result(path: Path, error: Error = NoError, init: RequestedPathResultBuilder.() -> Unit) {
        val builder = RequestedPathResultBuilder(path, error)
        results.add(builder)
        builder.init()
    }

    fun result(path: String, error: Error = NoError, init: RequestedPathResultBuilder.() -> Unit) {
        val builder = RequestedPathResultBuilder(Path(path), error)
        results.add(builder)
        builder.init()
    }

    override fun buildResponse() = Response(
        get_resp = GetResp(req_path_results = results.map { it.build() })
    )
}

class RequestedPathResultBuilder internal constructor(
    private val requestedPath: Path,
    val error: Error = NoError
) {

    private val resolvedPaths = mutableListOf<ResolvedPathResultBuilder>()

    fun resolvedPath(path: Path, init: ResolvedPathResultBuilder.() -> Unit) {
        val builder = ResolvedPathResultBuilder(path)
        resolvedPaths.add(builder)
        builder.init()
    }

    fun resolvedPath(path: String, init: ResolvedPathResultBuilder.() -> Unit) {
        val builder = ResolvedPathResultBuilder(Path(path))
        resolvedPaths.add(builder)
        builder.init()
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

//class GetSupportedDMRespBuilder internal constructor(messageId: String) :
//    ResponseMessageBuilder(Header.MsgType.GET_SUPPORTED_DM_RESP, messageId) {
//
//    internal constructor(request: Msg) : this(request.id) {
//        request.requireType(Header.MsgType.GET_SUPPORTED_DM_RESP)
//    }
//
//    override fun buildResponse(): Response {
//        return Response(get_supported_dm_resp = GetSupportedDMResp())
//    }
//}