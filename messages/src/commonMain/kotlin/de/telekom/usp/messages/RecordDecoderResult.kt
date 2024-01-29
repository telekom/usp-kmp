package de.telekom.usp.messages

import de.telekom.usp.Error
import de.telekom.usp.proto.msg.Msg

sealed class RecordDecoderResult {

    /**
     * Signals that a USP message was successfully delivered to this controller.
     *
     * @property msg the message that was received
     */
    data class Message(val msg: Msg) : RecordDecoderResult()

    /**
     * Signals that an internal error has happened.
     *
     * @param cause the exception identifying the reason of the failure
     */
    data class DecoderError(val cause: Throwable) : RecordDecoderResult()

    /**
     * Signals that an error should be returned to the remote party.
     *
     * @property error the USP error to return to the remote party
     */
    data class UspError(val error: Error) : RecordDecoderResult()

    /**
     * Signals that the remote party has established a new session.
     *
     * TODO: do we really need this signal or should we keep the information internal to RecordDecoderImpl?
     *
     * @property sessionContext the newly created session context
     * @property previousSessionContext in case a re-establishing a session context containt the
     *           old session, otherwise `null`
     */
    data class SessionEstablished(
        val sessionContext: SessionContext,
        val previousSessionContext: SessionContext? = null
    ) : RecordDecoderResult() {

        val isRestarted: Boolean
            get() = previousSessionContext != null
    }

    /**
     * Signals that the remote party wants the specified record to be retransmitted to it.
     *
     * @property sessionId the session ID of the record to retransmit to the remote party
     * @property sequenceId the sequence ID of the record to retransmit to the remote party
     */
    data class Retransmit(val sessionId: Long, val sequenceId: Long) : RecordDecoderResult()

    /**
     * Signals that the record decoder has identified one or more missing records and that this
     * controller should probably send a request to the remote party to retransmit the specified
     * records.
     *
     * @property sessionId the session ID of the record to request retransmit for
     * @property sequenceIds the sequence IDs of the records to request retransmit for
     */
    data class RecordsMissing(val sessionId: Long, val sequenceIds: List<Long>) :
        RecordDecoderResult()

    /**
     * Signals the reception of a web socket connect record.
     */
    data object WebSocketConnect : RecordDecoderResult()

    /**
     * Signals the reception of a MQTT connect record.
     *
     * @property version the version string of the MQTT connect record
     * @property subscribedTopic the topic of the MQTT connect record
     */
    data class MqttConnect(val version: String, val subscribedTopic: String) : RecordDecoderResult()

    /**
     * Signals the reception of a STOMP connect record.
     *
     * @property version the version string of the STMOP connect record
     * @param subscribedDestination the destination of the STMOP connect record
     */
    data class StompConnect(val version: String, val subscribedDestination: String) :
        RecordDecoderResult()

    /**
     * Signals the reception of a unix domain socket connect record.
     */
    data object UdsConnect : RecordDecoderResult()

    /**
     * Signals the reception of a disconnect record.
     *
     * @property error the error the remote party sent in the disconnect record
     */
    data class Disconnect(val error: Error) : RecordDecoderResult()
}