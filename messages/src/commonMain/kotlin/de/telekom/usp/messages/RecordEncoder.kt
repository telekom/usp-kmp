package de.telekom.usp.messages

import de.telekom.usp.Error
import de.telekom.usp.proto.msg.Msg
import okio.ByteString

interface RecordEncoder {

    /**
     * Encode a USP message in a no session context record
     */
    fun noSessionContextMessage(msg: Msg): ByteString

    /**
     * Encode a session context message with the specified session ID and an optional message and
     * an optional retransmit (sequence) ID.
     */
    fun sessionContextMessage(
        sessionId: Long,
        msg: Msg? = null,
        retransmitId: Long? = null
    ): ByteString

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