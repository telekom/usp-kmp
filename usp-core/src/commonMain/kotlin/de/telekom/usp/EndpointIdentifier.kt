/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp

private const val BBF_NAMESPACE = "urn:bbf:usp:id"

data class EndpointIdentifier(
    val scheme: AuthorityScheme,
    val authorityId: AuthorityId,
    val instanceId: InstanceId
) {
    /**
     * Returns this endpoint identifier prepended with the BBF namespace (`urn:bbf:usp:id:`).
     */
    fun toLongString() =
        "$BBF_NAMESPACE:${scheme.value}:${authorityId}:${instanceId}"

    /**
     * Returns this endpoint identifier without a prepended BBF namespace (`urn:bbf:usp:id:`).
     */
    fun toShortString() = "${scheme.value}:${authorityId}:${instanceId}"

    override fun toString() = toShortString()
}

/**
 * Factory method to convert a text into a `EndpointIdentifier`.
 *
 * @throws IllegalArgumentException when the text does not represent a valid endpoint identifier.
 */
fun EndpointIdentifier(text: String): EndpointIdentifier {
    val parts = (if (text.startsWith(BBF_NAMESPACE)) {
        text.substringAfter("$BBF_NAMESPACE:")
    } else {
        text
    }).split(":")

    require(parts.size == 3) { "Invalid endpoint identifier: '$text' (3 parts expected)" }

    val (scheme, authority, instance) = parts

    val authorityScheme = AuthorityScheme.from(scheme)
    requireNotNull(authorityScheme) { "Invalid endpoint identifier: '$text' (unknown scheme: '$scheme')" }
    require(authorityScheme.isValidAuthority(authority)) { "Invalid endpoint identifier: '$text' (invalid authority-id: '$authority')" }
    require(InstanceId.isValidId(instance)) { "Invalid endpoint identifier: '$text' (invalid instance-id: '$instance')" }

    return EndpointIdentifier(authorityScheme, AuthorityId(authority), InstanceId(instance))
}

fun String.toEndpoint(): EndpointIdentifier = EndpointIdentifier(this)
