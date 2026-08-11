/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp

import de.telekom.usp.internal.PathParser

/**
 * Represents a supported data model path, i.e. a path which may(!) contain "{i}" elements in it.
 */
interface SupportedDataModelPath : Path

fun SupportedDataModelPath(text: String): SupportedDataModelPath {
    return PathParser(text).parse(asSupportedDataModelPath = true) as SupportedDataModelPath
}
