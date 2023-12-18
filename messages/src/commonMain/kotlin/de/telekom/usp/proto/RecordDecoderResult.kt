package de.telekom.usp.proto

sealed class RecordDecoderResult {

    data class Error(val error: de.telekom.usp.Error) : RecordDecoderResult()

    data class RestartSession(val sessionId: Long) : RecordDecoderResult()
}