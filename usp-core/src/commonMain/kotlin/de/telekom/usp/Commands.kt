/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp

import de.telekom.usp.PathElement.Command
import de.telekom.usp.internal.ResolvedPathImpl

// All Device. commands:
val Reboot: ResolvedPath = ResolvedPathImpl(Device.first(), Command("Reboot()"))
val FactoryReset: ResolvedPath = ResolvedPathImpl(Device.first(), Command("FactoryReset()"))
val SelfTestDiagnostics: ResolvedPath =
    ResolvedPathImpl(Device.first(), Command("SelfTestDiagnostics()"))
val PacketCaptureDiagnostics: ResolvedPath =
    ResolvedPathImpl(Device.first(), Command("PacketCaptureDiagnostics()"))
val ScheduleTimer: ResolvedPath = ResolvedPathImpl(Device.first(), Command("ScheduleTimer()"))