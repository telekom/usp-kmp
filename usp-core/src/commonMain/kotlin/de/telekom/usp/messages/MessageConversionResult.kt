/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.messages

import de.telekom.usp.Error
import de.telekom.usp.messages.proto.Msg
import kotlinx.datetime.Instant

sealed class MessageConversionResult {

    /**
     * Signals that a USP message was successfully delivered to this controller.
     *
     * @property msg the message that was received
     */
    data class Message(val msg: Msg) : MessageConversionResult()

    /**
     * Signals that an internal error has happened.
     *
     * @param cause the exception identifying the reason of the failure
     */
    data class DecoderError(val cause: Throwable) : MessageConversionResult()

    /**
     * Signals that an error should be returned to the remote party.
     *
     * @property error the USP error to return to the remote party
     */
    data class UspError(val error: Error) : MessageConversionResult()

    /**
     * Signals that the remote party has established a new session.
     */
    data class SessionEstablished(val isRestarted: Boolean, val creationTime: Instant) :
        MessageConversionResult()

    /**
     * Signals that the remote party wants the specified record to be retransmitted to it.
     *
     * @property sessionId the session ID of the record to retransmit to the remote party
     * @property sequenceId the sequence ID of the record to retransmit to the remote party
     */
    data class Retransmit(val sessionId: Long, val sequenceId: Long) : MessageConversionResult()

    /**
     * Signals that the record decoder has identified one or more missing records and that this
     * controller should probably send a request to the remote party to retransmit the specified
     * records.
     *
     * @property sessionId the session ID of the record to request retransmit for
     * @property sequenceIds the sequence IDs of the records to request retransmit for
     */
    data class RecordsMissing(val sessionId: Long, val sequenceIds: List<Long>) :
        MessageConversionResult()

    /**
     * Signals the reception of a web socket connect record.
     */
    data object WebSocketConnect : MessageConversionResult()

    /**
     * Signals the reception of a MQTT connect record.
     *
     * @property version the version string of the MQTT connect record
     * @property subscribedTopic the topic of the MQTT connect record
     */
    data class MqttConnect(val version: String, val subscribedTopic: String) :
        MessageConversionResult()

    /**
     * Signals the reception of a STOMP connect record.
     *
     * @property version the version string of the STMOP connect record
     * @param subscribedDestination the destination of the STMOP connect record
     */
    data class StompConnect(val version: String, val subscribedDestination: String) :
        MessageConversionResult()

    /**
     * Signals the reception of a unix domain socket connect record.
     */
    data object UdsConnect : MessageConversionResult()

    /**
     * Signals the reception of a disconnect record.
     *
     * @property error the error the remote party sent in the disconnect record
     */
    data class Disconnect(val error: Error) : MessageConversionResult()
}