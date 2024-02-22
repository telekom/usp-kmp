package de.telekom.usp.mtp

import de.telekom.usp.EndpointConnection
import okio.ByteString

sealed class ConnectionEvent {

    /**
     * Indicates that the `EndpointConnection` is successfully connected to its remote host.
     */
    data class Connected(val to: EndpointConnection) : ConnectionEvent()

    /**
     * Indicates that the `EndpointConnection` has been (intentionally) disconnected from its remote
     * host.
     */
    data class Disconnected(val from: EndpointConnection) : ConnectionEvent()

    /**
     * Indicates that the `EndpointConnection` cannot establish a connection to its remote host.
     */
    data class ConnectionFailed(val from: EndpointConnection, val reason: Throwable?) :
        ConnectionEvent()

    /**
     * Indicates that the `EndpointConnection` received a byte string from the remote host.
     */
    data class BytesReceived(val bytes: ByteString) : ConnectionEvent()
}