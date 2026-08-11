/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp

import kotlin.test.Test
import kotlin.test.assertIs

class SupportedDataModelPathTest {

    @Test
    fun `creation of supported data model path`() {
        listOf(
            "Device.WiFi.",
            "Device.WiFi.Radio.{i}.Stats",
            "Device.WiFi.SSID.{i}.",
            "Device.WiFi.AccessPoint.{i}.AC.{i}.Stats."
        ).forEach { text ->
            val path = SupportedDataModelPath(text)
            assertIs<SupportedDataModelPath>(path)
        }
    }
}