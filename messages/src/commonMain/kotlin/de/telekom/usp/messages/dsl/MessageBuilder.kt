package de.telekom.usp.messages.dsl

import de.telekom.usp.proto.msg.Header
import de.telekom.usp.proto.msg.Msg

// See: https://kotlinlang.org/docs/type-safe-builders.html#scope-control-dslmarker
@DslMarker
annotation class MessageDslMarker

@MessageDslMarker
abstract class MessageBuilder internal constructor(val type: Header.MsgType) {

    var messageId: String? = null

    abstract fun build(): Msg
}
