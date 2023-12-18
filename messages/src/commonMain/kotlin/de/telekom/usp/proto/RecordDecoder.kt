package de.telekom.usp.proto

import kotlinx.coroutines.flow.SharedFlow
import okio.ByteString

/**
 * Handles decoding of USP records. The decoding results are published to a `SharedFlow` of
 * [RecordDecoderResult] instances.
 *
 * @see RecordDecoderResult
 */
interface RecordDecoder {

    val results: SharedFlow<RecordDecoderResult>

    suspend fun next(data: ByteString)
}
