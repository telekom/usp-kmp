/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp

import de.telekom.usp.util.isHexDigit

enum class AuthorityScheme(val value: String, internal val isValidAuthority: (String) -> Boolean) {

    OUI("oui", hexValidator),
    CID("cid", hexValidator),
    PEN("pen", { text -> text.toLongOrNull() != null }),
    SELF("self", zeroToSixCharsValidator),
    USER("user", zeroToSixCharsValidator),
    OS("os", emptyStringValidator),
    OPS("ops", emptyStringValidator),
    UUID("uuid", emptyStringValidator),
    IMEI("imei", emptyStringValidator),
    PROTO("proto", zeroToSixCharsValidator),
    DOC("doc", zeroToSixCharsValidator),
    FQDN("fqdn", emptyStringValidator);

    companion object {

        fun from(text: String) = entries.firstOrNull { scheme -> scheme.value == text }
    }
}

private val emptyStringValidator: (String) -> Boolean = { authority ->
    authority.isEmpty()
}

private val zeroToSixCharsValidator: (String) -> Boolean = { authority ->
    authority.length in 0..6
}

private val hexValidator: (String) -> Boolean = { authority ->
    authority.length == 6 && authority.all { c -> c.isHexDigit() }
}

