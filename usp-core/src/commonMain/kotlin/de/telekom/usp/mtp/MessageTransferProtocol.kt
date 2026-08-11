/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.mtp

/**
 * The message transfer protocols defined in the USP specification chapter 4. Note that as CoAP is
 * deprecated in USP 1.2, it is deliberately not included here anymore.
 */
@Suppress("unused")
enum class MessageTransferProtocol {

    WEB_SOCKET,
    STOMP,
    MQTT,
    UDS
}