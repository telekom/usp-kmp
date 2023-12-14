package de.telekom.usp

import de.telekom.usp.PathElement.*

// All Device. commands:
val Reboot = Path(Device.first(), Command("Reboot()"))
val FactoryReset = Path(Device.first(), Command("FactoryReset()"))
val SelfTestDiagnostics = Path(Device.first(), Command("SelfTestDiagnostics()"))
val PacketCaptureDiagnostics = Path(Device.first(), Command("PacketCaptureDiagnostics()"))
val ScheduleTimer = Path(Device.first(), Command("ScheduleTimer()"))