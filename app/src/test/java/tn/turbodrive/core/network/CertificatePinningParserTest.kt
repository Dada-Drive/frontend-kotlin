package tn.turbodrive.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CertificatePinningParserTest {
    // ── Valid single-host inputs ─────────────────────────────────────────────

    @Test
    fun `single host with single pin returns one pair`() {
        val result = parseCertificatePins("api.example.com|sha256/AAA=")
        assertEquals(listOf("api.example.com" to "sha256/AAA="), result)
    }

    @Test
    fun `same host with two comma-separated pins returns two pairs`() {
        val result = parseCertificatePins("api.example.com|sha256/AAA=,api.example.com|sha256/BBB=")
        assertEquals(
            listOf(
                "api.example.com" to "sha256/AAA=",
                "api.example.com" to "sha256/BBB=",
            ),
            result,
        )
    }

    @Test
    fun `same host with two pipe-grouped pins returns two pairs`() {
        val result = parseCertificatePins("api.example.com|sha256/AAA=|sha256/BBB=")
        assertEquals(
            listOf(
                "api.example.com" to "sha256/AAA=",
                "api.example.com" to "sha256/BBB=",
            ),
            result,
        )
    }

    // ── Valid multi-host inputs ──────────────────────────────────────────────

    @Test
    fun `two hosts with one pin each returns two pairs`() {
        val result = parseCertificatePins("staging.example.com|sha256/AAA=,api.example.com|sha256/BBB=")
        assertEquals(
            listOf(
                "staging.example.com" to "sha256/AAA=",
                "api.example.com" to "sha256/BBB=",
            ),
            result,
        )
    }

    // ── Edge cases on whitespace and empties ────────────────────────────────

    @Test
    fun `empty string returns empty list`() {
        assertTrue(parseCertificatePins("").isEmpty())
    }

    @Test
    fun `blank string returns empty list`() {
        assertTrue(parseCertificatePins("    ").isEmpty())
    }

    @Test
    fun `whitespace around tokens is trimmed`() {
        val result = parseCertificatePins("   api.example.com  |  sha256/AAA=  ")
        assertEquals(listOf("api.example.com" to "sha256/AAA="), result)
    }

    // ── Malformed entries are skipped, not exceptional ──────────────────────

    @Test
    fun `entry without pipe separator is skipped`() {
        val result = parseCertificatePins("api.example.com|sha256/AAA=,malformed")
        assertEquals(listOf("api.example.com" to "sha256/AAA="), result)
    }

    @Test
    fun `entry with only a host and no pin is skipped`() {
        val result = parseCertificatePins("api.example.com|sha256/AAA=,onlyHost|")
        assertEquals(listOf("api.example.com" to "sha256/AAA="), result)
    }

    // ── Parser does NOT validate pin format (delegated to OkHttp) ───────────

    @Test
    fun `parser accepts pin missing sha256 prefix (OkHttp validates later)`() {
        val result = parseCertificatePins("api.example.com|XYZ")
        assertEquals(listOf("api.example.com" to "XYZ"), result)
    }
}
