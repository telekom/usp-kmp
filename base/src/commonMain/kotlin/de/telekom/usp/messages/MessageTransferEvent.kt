package de.telekom.usp.messages

import okio.ByteString

sealed class MessageTransferEvent {

    /**
     * Indicates that the `EndpointConnection` is successfully connected to its remote host.
     */
    data class Connected(val to: MessageTransfer) : MessageTransferEvent()

    /**
     * Indicates that the `EndpointConnection` has been (intentionally) disconnected from its remote
     * host.
     */
    data class Disconnected(val from: MessageTransfer) : MessageTransferEvent()

    /**
     * Indicates that the `EndpointConnection` cannot establish a connection to its remote host.
     */
    data class ConnectionFailed(val from: MessageTransfer, val reason: Throwable?) :
        MessageTransferEvent()

    /**
     * Indicates that the `EndpointConnection` received a byte string from the remote host.
     */
    data class BytesReceived(val bytes: ByteString) : MessageTransferEvent()
}