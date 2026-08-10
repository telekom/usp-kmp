/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.cli

import de.telekom.usp.e2e.MessageExchange
import kotlin.time.Duration

class CommandContext(val exchange: MessageExchange, val timeout: Duration)