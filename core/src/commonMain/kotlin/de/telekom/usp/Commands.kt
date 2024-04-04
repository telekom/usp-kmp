package de.telekom.usp

import de.telekom.usp.PathElement.Command
import de.telekom.usp.internal.PathImpl

// All Device. commands:
val Reboot: Path = PathImpl(Device.first(), Command("Reboot()"))
val FactoryReset: Path = PathImpl(Device.first(), Command("FactoryReset()"))
val SelfTestDiagnostics: Path = PathImpl(Device.first(), Command("SelfTestDiagnostics()"))
val PacketCaptureDiagnostics: Path = PathImpl(Device.first(), Command("PacketCaptureDiagnostics()"))
val ScheduleTimer: Path = PathImpl(Device.first(), Command("ScheduleTimer()"))