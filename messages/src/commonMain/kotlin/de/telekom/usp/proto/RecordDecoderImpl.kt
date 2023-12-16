package de.telekom.usp.proto

import de.telekom.usp.MessageNotSupported
import de.telekom.usp.Versions
import de.telekom.usp.proto.record.Record
import kotlinx.coroutines.channels.Channel
import okio.ByteString

class RecordDecoderImpl : RecordDecoder {

    val results = Channel<RecordDecoderResult>(Channel.CONFLATED)

    override suspend fun next(recordData: ByteString) {
        val record = Record.ADAPTER.decode(recordData)

        if (!Versions.isSupported(record.version)) {
            results.send(RecordDecoderResult.DecoderError(MessageNotSupported))
        }
    }
}