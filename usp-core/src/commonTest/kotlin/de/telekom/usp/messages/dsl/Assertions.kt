/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.telekom.usp.messages.dsl

import de.telekom.usp.messages.proto.Header
import de.telekom.usp.messages.proto.Msg
import kotlin.test.assertNotNull
import kotlin.test.assertSame


fun assertMessageType(expected: Header.MsgType, actual: Msg) {
    assertNotNull(actual)
    assertNotNull(actual.header_)
    assertNotNull(actual.header_!!.msg_id)
    assertSame(expected, actual.header_!!.msg_type, "Wrong type in Msg")
}
