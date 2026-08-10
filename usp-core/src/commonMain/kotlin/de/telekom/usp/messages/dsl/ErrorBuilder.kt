/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

@file:Suppress("FunctionName")

package de.telekom.usp.messages.dsl

import de.telekom.usp.Path
import de.telekom.usp.messages.MessageIdFactory
import de.telekom.usp.messages.proto.Body
import de.telekom.usp.messages.proto.Error
import de.telekom.usp.messages.proto.Header
import de.telekom.usp.messages.proto.Msg

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

    private val params = mutableListOf<Error.ParamError>()

    fun parameter(path: Path, error: de.telekom.usp.Error) {
        params.add(Error.ParamError(path.toString(), error.code, error.name))
    }

    fun parameter(path: Path, errorCode: Int, errorMessage: String) {
        params.add(Error.ParamError(path.toString(), errorCode, errorMessage))
    }

    fun parameter(path: String, error: de.telekom.usp.Error) {
        params.add(Error.ParamError(path, error.code, error.name))
    }

    fun parameter(path: String, errorCode: Int, errorMessage: String) {
        params.add(Error.ParamError(path, errorCode, errorMessage))
    }

    override fun build(): Msg {
        return Msg(
            header_ = Header(
                msg_type = Header.MsgType.ERROR,
                msg_id = messageId ?: MessageIdFactory.next("ERROR-")
            ),
            body = Body(
                error = Error(
                    err_msg = errorMessage,
                    err_code = errorCode,
                    param_errs = params
                )
            )
        )
    }
}