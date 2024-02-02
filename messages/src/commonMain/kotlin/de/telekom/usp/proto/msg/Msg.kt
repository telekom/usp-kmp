package de.telekom.usp.proto.msg

val Msg.id: String
    get() = header_?.msg_id ?: "UNDEFINED"