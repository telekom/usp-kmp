/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.mtp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.ByteString

abstract class AbstractMessageTransfer : MessageTransfer {

    private val _events = MutableSharedFlow<MessageTransferEvent>()
    override val events: SharedFlow<MessageTransferEvent>
        get() = _events.asSharedFlow()

    protected val inputBuffer =
        MutableSharedFlow<ByteString>(replay = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val mutex = Mutex()

    private var isConnected = false

    override suspend fun send(bytes: ByteString) {
        inputBuffer.emit(bytes)
    }

    protected suspend fun isConnected(): Boolean {
        mutex.withLock {
            return isConnected
        }
    }

    protected suspend fun setConnected(isConnected: Boolean) {
        mutex.withLock {
            this.isConnected = isConnected
            Logger.d { "New state of $this is: ${if (isConnected) "CONNECTED" else "DISCONNECTED"}" }
        }
    }

    protected suspend fun emit(event: MessageTransferEvent) {
        _events.emit(event)
    }
}