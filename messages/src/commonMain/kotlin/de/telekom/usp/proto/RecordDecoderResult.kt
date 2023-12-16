package de.telekom.usp.proto

import de.telekom.usp.Error

sealed class RecordDecoderResult {

    data class DecoderError(val error: Error) : RecordDecoderResult()
}