package de.telekom.usp.proto

import kotlinx.coroutines.flow.SharedFlow
import okio.ByteString


interface RecordDecoder {

    val result: SharedFlow<RecordDecoderResult>

    suspend fun next(recordData: ByteString)
}
