package de.telekom.usp.messages

internal enum class SessionContextState {
    NONE,
    REQUESTED,
    CONNECTING,
    ESTABLISHED,
    ERROR;
}