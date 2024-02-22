package de.telekom.usp.messages.dsl

import de.telekom.usp.messages.proto.Header
import de.telekom.usp.messages.proto.Msg

// See: https://kotlinlang.org/docs/type-safe-builders.html#scope-control-dslmarker
@DslMarker
annotation class MessageDslMarker

@MessageDslMarker
abstract class MessageBuilder internal constructor(val type: Header.MsgType) {

    var messageId: String? = null

    abstract fun build(): Msg
}

internal fun <T : MessageBuilder> initBuilder(builder: T, init: T.() -> Unit): Msg {
    builder.init()
    return builder.build()
}