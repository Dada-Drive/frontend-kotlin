package tn.turbodrive.data.local

import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import tn.turbodrive.data.local.dao.CachedActiveRideDao
import tn.turbodrive.data.local.entity.CachedActiveRideEntity
import tn.turbodrive.domain.models.ActiveRide
import tn.turbodrive.domain.models.RideStatus

class ActiveRideDraftCacheTest {
    private val dao = mockk<CachedActiveRideDao>(relaxed = true)
    private val db =
        mockk<AppDatabase>(relaxed = true) {
            every { cachedActiveRideDao() } returns dao
        }
    private val cache = ActiveRideDraftCache(db, Gson())

    private val acceptedRide =
        ActiveRide(
            id = "r1",
            riderName = "Sami",
            riderPhone = "+216",
            pickupAddress = "Tunis",
            dropoffAddress = "La Marsa",
            pickupLat = 36.8065,
            pickupLng = 10.1815,
            dropoffLat = 36.81,
            dropoffLng = 10.19,
            distanceKm = 3.0,
            estimatedMinutes = 8,
            finalFare = null,
            calculatedFare = 8.5,
            status = RideStatus.Accepted,
            startedAt = null,
            completedAt = null,
        )

    @Test
    fun `load returns null when dao has no cached entry`() =
        runTest {
            coEvery { dao.getSingleton() } returns null

            val result = cache.load()

            assertNull(result)
        }

    @Test
    fun `load returns ride matching the JSON saved by saveActiveOrClear`() =
        runTest {
            val json = Gson().toJson(acceptedRide)
            coEvery { dao.getSingleton() } returns
                CachedActiveRideEntity(
                    rideStateJson = json,
                    cachedAt = System.currentTimeMillis(),
                )

            val result = cache.load()

            assertNotNull(result)
            assertEquals(acceptedRide.id, result!!.id)
            assertEquals(acceptedRide.status, result.status)
        }

    @Test
    fun `saveActiveOrClear with completed ride clears cache instead of upserting`() =
        runTest {
            val completedRide = acceptedRide.copy(status = RideStatus.Completed)

            cache.saveActiveOrClear(completedRide)

            coVerify { dao.clearAll() }
            coVerify(exactly = 0) { dao.upsert(any()) }
        }
}
