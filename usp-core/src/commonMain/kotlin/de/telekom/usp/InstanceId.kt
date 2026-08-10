/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp

import de.telekom.usp.util.isHexDigit
import de.telekom.usp.util.isUnreserved
import kotlin.jvm.JvmInline

@JvmInline
value class InstanceId(private val instance: String) {

    override fun toString(): String {
        return instance
    }

    companion object {

        fun isValidId(instance: String): Boolean {
            // R-ARC.6 - An instance-id value MUST be no more than 50 characters in length.
            if (instance.length > 50) {
                return false
            }

            // R-ARC.5 instance ID must consist mainly of alpha, digits and "-" / "." / "_" or hex
            var i = 0
            while (i < instance.length) {
                if (instance[i].isUnreserved()) {
                    i += 1
                    continue
                } else if (instance[i] == '%' && i < instance.length - 2) {
                    if (instance[i + 1].isHexDigit() && instance[i + 2].isHexDigit()) {
                        i += 3
                        continue
                    }
                }
                return false
            }
            return true
        }
    }
}