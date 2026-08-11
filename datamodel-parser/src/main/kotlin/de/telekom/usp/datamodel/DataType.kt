/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.telekom.usp.datamodel

data class DataType(
    val name: String,
    val base: String?,
    val type: String?,
    val description: String?,
    val enumerations: List<Enumeration>,
    val patterns: List<String>
) {
    val hasCode: Boolean
        get() = enumerations.isNotEmpty() && enumerations[0].code != null
}