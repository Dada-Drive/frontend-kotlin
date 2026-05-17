package tn.turbodrive.core.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tn.turbodrive.presentation.driversetup.parseUnderscoreDate

class DateParseResultTest {
    // ── Valid inputs ─────────────────────────────────────────────────────────

    @Test
    fun `parseUnderscoreDate returns Valid for canonical January date`() {
        val result = parseUnderscoreDate("01_01_2025")
        assertEquals(DateParseResult.Valid("2025-01-01"), result)
    }

    @Test
    fun `parseUnderscoreDate returns Valid for leap-year Feb 29`() {
        val result = parseUnderscoreDate("29_02_2024")
        assertEquals(DateParseResult.Valid("2024-02-29"), result)
    }

    @Test
    fun `parseUnderscoreDate returns Valid for end-of-year date`() {
        val result = parseUnderscoreDate("31_12_2099")
        assertEquals(DateParseResult.Valid("2099-12-31"), result)
    }

    // ── Invalid: out-of-range components ─────────────────────────────────────

    @Test
    fun `parseUnderscoreDate returns Invalid when day exceeds 31`() {
        assertEquals(DateParseResult.Invalid, parseUnderscoreDate("32_01_2025"))
    }

    @Test
    fun `parseUnderscoreDate returns Invalid when month exceeds 12`() {
        assertEquals(DateParseResult.Invalid, parseUnderscoreDate("15_13_2025"))
    }

    @Test
    fun `parseUnderscoreDate returns Invalid for Feb 29 in non-leap year`() {
        assertEquals(DateParseResult.Invalid, parseUnderscoreDate("29_02_2025"))
    }

    @Test
    fun `parseUnderscoreDate returns Invalid when day is zero`() {
        assertEquals(DateParseResult.Invalid, parseUnderscoreDate("00_01_2025"))
    }

    @Test
    fun `parseUnderscoreDate returns Invalid when month is zero`() {
        assertEquals(DateParseResult.Invalid, parseUnderscoreDate("01_00_2025"))
    }

    // ── Invalid: malformed strings ───────────────────────────────────────────

    @Test
    fun `parseUnderscoreDate returns Invalid for non-numeric input`() {
        assertEquals(DateParseResult.Invalid, parseUnderscoreDate("abc_def_ghij"))
    }

    @Test
    fun `parseUnderscoreDate returns Invalid when format is too short`() {
        // Helper extracts digits only; 6-digit "1_1_2025" -> 6 digits, < 8 required.
        assertEquals(DateParseResult.Invalid, parseUnderscoreDate("1_1_2025"))
    }

    @Test
    fun `parseUnderscoreDate returns Invalid for empty string`() {
        assertEquals(DateParseResult.Invalid, parseUnderscoreDate(""))
    }

    // ── Sealed-type smoke ────────────────────────────────────────────────────

    @Test
    fun `Valid and Invalid are distinct sealed-type instances`() {
        val valid: DateParseResult = DateParseResult.Valid("2025-01-01")
        val invalid: DateParseResult = DateParseResult.Invalid
        assertNotEquals(valid, invalid)
        assertTrue(valid is DateParseResult.Valid)
        assertTrue(invalid === DateParseResult.Invalid)
    }
}
