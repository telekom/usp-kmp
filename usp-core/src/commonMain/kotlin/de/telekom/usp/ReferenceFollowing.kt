/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp

/**
 * Represents a reference following directive.
 *
 * @property name text of the reference to follow, for example for `ReferenceParameter#2+` this
 *           would be "ReferenceParameter"
 * @property itemNumber the item number to follow or 0 (zero) to follow all references (wildcard)
 */
data class ReferenceFollowing(val name: String, val itemNumber: Int) {

    val followAll = itemNumber == 0

    companion object {

        fun from(text: String): ReferenceFollowing? {
            val matcher = Regex.find(text)

            return if (matcher == null) {
                null
            } else {
                val name = matcher.groups[1]!!.value
                when (val item = matcher.groups[3]?.value) {
                    null -> {
                        // For "ReferenceParameter+" match group 3 is null
                        ReferenceFollowing(name, 1)
                    }

                    "*" -> {
                        ReferenceFollowing(name, 0)
                    }

                    else -> {
                        ReferenceFollowing(name, item.toInt())
                    }
                }
            }
        }

        private val Regex = """^([^+#]+)(#(\*|\d+))?\+$""".toRegex()
    }
}