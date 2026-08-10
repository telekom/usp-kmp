/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.e2e

import de.telekom.usp.messages.proto.Error

sealed class MessageExchangeFailure {

    data class ResponseError(val error: Error) : MessageExchangeFailure()

    data class TimeoutOccurred(val messageId: String) : MessageExchangeFailure()

    data object ConnectionFailed : MessageExchangeFailure()
}