package de.telekom.usp.mtp

import okio.ByteString

sealed class ConnectionEvent {

    data class Connected(val to: EndpointConnection) : ConnectionEvent()

    data class Disconnected(val from: EndpointConnection) : ConnectionEvent()

    data class BytesReceived(val bytes: ByteString) : ConnectionEvent()
}