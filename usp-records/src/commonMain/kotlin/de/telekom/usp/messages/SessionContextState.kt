/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.messages

internal enum class SessionContextState {
    NONE,
    REQUESTED,
    CONNECTING,
    ESTABLISHED,
    ERROR;
}