package de.telekom.usp

class WebSocketConnection(
    private val host: String,
    private val port: Int,
    private val to: EndpointIdentifier,
    private val from: EndpointIdentifier,
    private val developmentMode: Boolean = false
) {
}