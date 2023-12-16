package de.telekom.usp.proto

import okio.ByteString

interface RecordDecoder {

    suspend fun next(recordData: ByteString)
}