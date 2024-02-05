package de.telekom.usp.messages.dsl

import de.telekom.usp.InternalError
import de.telekom.usp.ParameterActionFailed
import de.telekom.usp.proto.msg.Header
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorBuilderTest {

    @Test
    fun `create Error message`() {
        val error = Error(InternalError) {
            parameter("Device.", 4711, "test-error")
            parameter("Device.UserInterface.", ParameterActionFailed)
        }

        assertMessageType(Header.MsgType.ERROR, error)
        assertEquals(InternalError.name, error.body!!.error!!.err_msg)
        assertEquals(InternalError.code, error.body!!.error!!.err_code)
        assertEquals(2, error.body!!.error!!.param_errs.size)
        assertEquals(4711, error.body!!.error!!.param_errs[0].err_code)
        assertEquals("test-error", error.body!!.error!!.param_errs[0].err_msg)
        assertEquals(ParameterActionFailed.code, error.body!!.error!!.param_errs[1].err_code)
        assertEquals(ParameterActionFailed.name, error.body!!.error!!.param_errs[1].err_msg)
    }
}