package de.telekom.usp.messages

import de.telekom.usp.Error
import de.telekom.usp.proto.msg.Msg

sealed class RecordDecoderResult {

    data class DecoderError(val cause: Throwable) : RecordDecoderResult()

    data class UspError(val error: Error) : RecordDecoderResult()

    data class Message(val msg: Msg) : RecordDecoderResult()

    data object WebSocketConnect : RecordDecoderResult()

    data class MqttConnect(val version: String, val subscribedTopic: String) : RecordDecoderResult()

    data class StompConnect(val version: String, val subscribedDestination: String) : RecordDecoderResult()

    data class Disconnect(val reason: String, val reasonCode: Int) : RecordDecoderResult()
}