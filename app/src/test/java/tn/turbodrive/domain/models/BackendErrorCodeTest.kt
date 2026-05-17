package tn.turbodrive.domain.models

import org.junit.Assert.assertEquals
import org.junit.Test

class BackendErrorCodeTest {
    @Test
    fun `fromString matches exact uppercase name`() {
        assertEquals(BackendErrorCode.VALIDATION_ERROR, BackendErrorCode.fromString("VALIDATION_ERROR"))
    }

    @Test
    fun `fromString is case-insensitive`() {
        assertEquals(BackendErrorCode.VALIDATION_ERROR, BackendErrorCode.fromString("validation_error"))
        assertEquals(BackendErrorCode.RIDE_NOT_FOUND, BackendErrorCode.fromString("Ride_Not_Found"))
    }

    @Test
    fun `fromString trims whitespace`() {
        assertEquals(BackendErrorCode.RIDE_NOT_FOUND, BackendErrorCode.fromString(" RIDE_NOT_FOUND "))
        assertEquals(BackendErrorCode.OTP_INVALID, BackendErrorCode.fromString("\tOTP_INVALID\n"))
    }

    @Test
    fun `fromString returns UNKNOWN on null`() {
        assertEquals(BackendErrorCode.UNKNOWN, BackendErrorCode.fromString(null))
    }

    @Test
    fun `fromString returns UNKNOWN on blank or empty`() {
        assertEquals(BackendErrorCode.UNKNOWN, BackendErrorCode.fromString(""))
        assertEquals(BackendErrorCode.UNKNOWN, BackendErrorCode.fromString("   "))
    }

    @Test
    fun `fromString returns UNKNOWN for unknown codes`() {
        // Future backend code shipped after this build : no crash, fallback safely.
        assertEquals(BackendErrorCode.UNKNOWN, BackendErrorCode.fromString("FUTURE_CODE_NOT_YET_DEFINED"))
        assertEquals(BackendErrorCode.UNKNOWN, BackendErrorCode.fromString("typo_in_code"))
    }
}
