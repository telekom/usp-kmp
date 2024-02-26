package de.telekom.usp

import de.telekom.usp.messages.proto.Error

sealed class MessageExchangeFailure {

    data class ResponseError(val error: Error) : MessageExchangeFailure()

    data class TimeoutOccurred(val messageId: String) : MessageExchangeFailure()
}