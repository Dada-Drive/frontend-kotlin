package tn.turbodrive.data.location

import android.content.Context
import android.location.Location
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LocationServiceControllerTest {
    private val context: Context = mockk(relaxed = true)
    private lateinit var controller: LocationServiceController

    @Before
    fun setUp() {
        controller = LocationServiceController(context)
    }

    private fun location(accuracy: Float): Location = mockk<Location>().also { every { it.accuracy } returns accuracy }

    // ── Mode state ────────────────────────────────────────────────────────────

    @Test
    fun `default mode is COARSE`() {
        assertEquals(GpsMode.COARSE, controller.gpsMode.value)
    }

    @Test
    fun `setMode changes state to HIGH`() {
        controller.setMode(GpsMode.HIGH)
        assertEquals(GpsMode.HIGH, controller.gpsMode.value)
    }

    @Test
    fun `setMode with same value is a no-op`() {
        controller.setMode(GpsMode.COARSE)
        controller.setMode(GpsMode.COARSE)
        assertEquals(GpsMode.COARSE, controller.gpsMode.value)
    }

    // ── Accuracy filter ───────────────────────────────────────────────────────

    @Test
    fun `shouldAcceptLocation rejects accuracy above 50m`() {
        assertFalse(controller.shouldAcceptLocation(location(accuracy = 100f)))
    }

    @Test
    fun `shouldAcceptLocation rejects accuracy at strict boundary 50_1m`() {
        assertFalse(controller.shouldAcceptLocation(location(accuracy = 50.1f)))
    }

    @Test
    fun `shouldAcceptLocation accepts first location with valid accuracy`() {
        assertTrue(controller.shouldAcceptLocation(location(accuracy = 10f)))
    }

    // ── Displacement filter ───────────────────────────────────────────────────

    @Test
    fun `shouldAcceptLocation rejects displacement below 8m`() {
        val first = location(accuracy = 10f)
        controller.shouldAcceptLocation(first)

        val second = location(accuracy = 10f)
        every { second.distanceTo(first) } returns 7f

        assertFalse(controller.shouldAcceptLocation(second))
    }

    @Test
    fun `shouldAcceptLocation accepts displacement above 8m`() {
        val first = location(accuracy = 10f)
        controller.shouldAcceptLocation(first)

        val second = location(accuracy = 10f)
        every { second.distanceTo(first) } returns 10f

        assertTrue(controller.shouldAcceptLocation(second))
    }

    // ── OFF mode ──────────────────────────────────────────────────────────────

    @Test
    fun `shouldAcceptLocation rejects all locations when mode is OFF`() {
        controller.setMode(GpsMode.OFF)
        assertFalse(controller.shouldAcceptLocation(location(accuracy = 5f)))
    }

    // ── resetLastLocation ─────────────────────────────────────────────────────

    @Test
    fun `resetLastLocation bypasses displacement filter for next location`() {
        val first = location(accuracy = 10f)
        controller.shouldAcceptLocation(first)
        controller.resetLastLocation()

        val second = location(accuracy = 10f)
        // distanceTo not mocked — must not be called after reset (no prev reference)
        assertTrue(controller.shouldAcceptLocation(second))
    }
}
