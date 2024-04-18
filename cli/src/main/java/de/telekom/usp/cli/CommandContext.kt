package de.telekom.usp.cli

import de.telekom.usp.MessageExchange
import kotlin.time.Duration

class CommandContext(val exchange: MessageExchange, val timeout: Duration)