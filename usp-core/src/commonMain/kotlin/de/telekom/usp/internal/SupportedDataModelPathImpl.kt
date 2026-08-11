/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.internal

import de.telekom.usp.PathElement
import de.telekom.usp.ResolvedPath
import de.telekom.usp.SupportedDataModelPath

internal class SupportedDataModelPathImpl(elements: List<PathElement>) : PathImpl(elements),
    SupportedDataModelPath {

    override val isResolved = false

    override fun asResolvedPath(): ResolvedPath {
        throw IllegalArgumentException("SupportedDataModelPath cannot be converted into a ResolvedPath")
    }
}