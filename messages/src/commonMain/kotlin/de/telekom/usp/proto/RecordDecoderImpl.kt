package de.telekom.usp.proto

import co.touchlab.kermit.Logger
import de.telekom.usp.MessageNotSupported
import de.telekom.usp.Versions
import de.telekom.usp.proto.record.Record
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okio.ByteString

class RecordDecoderImpl(private val context: SessionContext) : RecordDecoder {

    private val _result = MutableSharedFlow<RecordDecoderResult>()
    override val result = _result.asSharedFlow()

    override suspend fun next(recordData: ByteString) {
        val record = Record.ADAPTER.decode(recordData)

        if (!Versions.isSupported(record.version)) {
            Logger.d { "Rejecting USP record with unsupported version: '${record.version}' ($record)" }
            _result.emit(RecordDecoderResult.Error(MessageNotSupported))
            return
        }

        if (!context.isEndpointMatching(record)) {
            Logger.d { "[R-E2E.1] Ignoring USP record with wrong to-endpoint: expecting=${context.to}, received=$record" }
            return
        }
    }
}