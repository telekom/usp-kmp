@file:Suppress("FunctionName")

package de.telekom.usp.messages.dsl

import de.telekom.usp.Path
import de.telekom.usp.messages.MessageIdFactory
import de.telekom.usp.proto.msg.Body
import de.telekom.usp.proto.msg.Error
import de.telekom.usp.proto.msg.Header
import de.telekom.usp.proto.msg.Msg

fun Error(errorCode: Int, errorMessage: String, init: ErrorBuilder.() -> Unit) =
    initBuilder(ErrorBuilder(errorCode, errorMessage), init)

fun Error(error: de.telekom.usp.Error, init: ErrorBuilder.() -> Unit) =
    initBuilder(ErrorBuilder(error.code, error.name), init)

// --- Builder classes -----------------------------------------------------------------------------

class ErrorBuilder internal constructor(
    private val errorCode: Int,
    private val errorMessage: String
) :
    MessageBuilder(Header.MsgType.ERROR) {

    private val params = mutableListOf<ErrorParameter>()

    fun parameter(path: Path, error: de.telekom.usp.Error) {
        params.add(ErrorParameter(path, error.code, error.name))
    }

    fun parameter(path: Path, errorCode: Int, errorMessage: String) {
        params.add(ErrorParameter(path, errorCode, errorMessage))
    }

    fun parameter(path: String, error: de.telekom.usp.Error) {
        params.add(ErrorParameter(Path(path), error.code, error.name))
    }

    fun parameter(path: String, errorCode: Int, errorMessage: String) {
        params.add(ErrorParameter(Path(path), errorCode, errorMessage))
    }

    override fun build(): Msg {
        return Msg(
            header_ = Header(
                msg_type = Header.MsgType.ERROR,
                msg_id = messageId ?: "ERROR-${MessageIdFactory.next()}"
            ),
            body = Body(
                error = Error(
                    err_msg = errorMessage,
                    err_code = errorCode,
                    param_errs = params.map {
                        Error.ParamError(
                            it.path.toString(),
                            it.errorCode,
                            it.errorMessage
                        )
                    }
                )
            )
        )
    }
}

private data class ErrorParameter(val path: Path, val errorCode: Int, val errorMessage: String)
