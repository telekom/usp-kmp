/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.mtp.mqtt

enum class QoS(val value: Int) {

    AT_MOST_ONCE(0),
    AT_LEAST_ONCE(1),
    EXACTLY_ONCE(2);

    companion object {

        fun valueOf(value: Int) = entries.firstOrNull { it.value == value }
    }
}