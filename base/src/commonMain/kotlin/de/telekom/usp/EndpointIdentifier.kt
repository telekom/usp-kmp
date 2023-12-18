package de.telekom.usp

private const val BBF_NAMESPACE = "urn:bbf:usp:id"

data class EndpointIdentifier(
    val scheme: AuthorityScheme,
    val authorityId: AuthorityId,
    val instanceId: InstanceId
) {

    fun toLongString() =
        "$BBF_NAMESPACE:${scheme.value}:${authorityId.authority}:${instanceId.instance}"

    fun toShortString() = "${scheme.value}:${authorityId.authority}:${instanceId.instance}"

    override fun toString() = toShortString()
}

fun EndpointIdentifier(text: String): EndpointIdentifier {
    val parts = (if (text.startsWith(BBF_NAMESPACE)) {
        text.substringAfter("$BBF_NAMESPACE:")
    } else {
        text
    }).split(":")

    require(parts.size == 3) { "Invalid endpoint identifier: '$text' (parts are missing)" }

    val (scheme, authority, instance) = parts

    val authorityScheme = AuthorityScheme.from(scheme)
    requireNotNull(authorityScheme) { "Invalid endpoint identifier: '$text' (unknown scheme: '$scheme')" }
    require(authorityScheme.isValidAuthority(authority)) { "Invalid endpoint identifier: '$text' (invalid authority-id: '$authority')" }
    require(InstanceId.isValidId(instance)) { "Invalid endpoint identifier: '$text' (invalid instance-id: '$instance')" }

    return EndpointIdentifier(authorityScheme, AuthorityId(authority), InstanceId(instance))
}
