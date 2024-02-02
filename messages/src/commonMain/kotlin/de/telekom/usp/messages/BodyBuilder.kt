package de.telekom.usp.messages

import de.telekom.usp.proto.msg.Header

@DslMarker
annotation class BodyDslMarker

@BodyDslMarker
open class BodyBuilder(val type: Header.MsgType) {

    var messageId: String? = null
}
