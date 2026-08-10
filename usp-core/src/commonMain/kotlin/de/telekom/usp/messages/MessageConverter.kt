/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.messages

import de.telekom.usp.Error
import de.telekom.usp.messages.proto.Msg
import kotlinx.coroutines.flow.SharedFlow
import okio.ByteString

/**
 * Handles encoding and decoding of USP messages to and from bytes, hiding the complexities of
 * handling USP records and end-to-end sessions.
 */
interface MessageConverter {

    /**
     * Reflects R-E2E.6a: an agent may not be allowed to establish a session context. In this case
     * the value of `allowSessionContext` must be `false`, otherwise it is `true`.
     */
    val allowSessionContext: Boolean

    val results: SharedFlow<MessageConversionResult>

    /**
     * Convert the specified bytes into a RecordDecoderResult.
     *
     * @see results
     */
    suspend fun next(data: ByteString)

    /**
     * Encode a session context message with the specified message and/or an optional sequence ID
     * to retransmit.
     */
    suspend fun sessionContextMessage(
        msg: Msg? = null,
        retransmitId: ULong? = null,
        useExistingSession: Boolean = true
    ): List<ByteString>

    /**
     * Encode a USP message in a no session context record
     */
    fun noSessionContextMessage(msg: Msg): ByteString

    /**
     * Encode a disconnect message using the reason of the specified error.
     */
    fun disconnect(error: Error): ByteString

    /**
     * Encode a web socket connect request.
     */
    fun webSocketConnect(): ByteString

    /**
     * Encode a MQTT connect request. Supported version strings are "3.1.1." and "5.0"
     */
    fun mqttConnect(mqttVersion: String, topic: String): ByteString

    /**
     * Encode a MQTT connect request. Currently the only supported version string is "1.2"
     */
    fun stompConnect(stompVersion: String, destination: String): ByteString

    /**
     * Encode a unix domain socket connect request.
     */
    fun udsConnect(): ByteString
}
