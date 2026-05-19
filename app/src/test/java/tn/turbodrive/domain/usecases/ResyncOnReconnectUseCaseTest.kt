package tn.turbodrive.domain.usecases

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import tn.turbodrive.domain.models.ActiveRide
import tn.turbodrive.domain.models.RideStatus
import tn.turbodrive.domain.models.WalletInfo
import tn.turbodrive.domain.protocols.RidesRepository
import tn.turbodrive.domain.protocols.WalletRepository

class ResyncOnReconnectUseCaseTest {
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

    private val sampleWallet =
        WalletInfo(
            id = "w1",
            ownerId = "u1",
            balance = 125.5,
            status = "active",
        )

    @Test
    fun `both calls succeed returns active ride and wallet`() =
        runTest {
            val rides = mockk<RidesRepository>()
            val wallet = mockk<WalletRepository>()
            coEvery { rides.getMyRides() } returns Result.success(listOf(acceptedRide))
            coEvery { wallet.getWallet() } returns Result.success(sampleWallet)

            val result = ResyncOnReconnectUseCase(rides, wallet)()

            assertNotNull(result.activeRide)
            assertEquals(acceptedRide.id, result.activeRide!!.id)
            assertEquals(sampleWallet.id, result.walletInfo!!.id)
        }

    @Test
    fun `rides call fails returns null activeRide with wallet`() =
        runTest {
            val rides = mockk<RidesRepository>()
            val wallet = mockk<WalletRepository>()
            coEvery { rides.getMyRides() } returns Result.failure(RuntimeException("network"))
            coEvery { wallet.getWallet() } returns Result.success(sampleWallet)

            val result = ResyncOnReconnectUseCase(rides, wallet)()

            assertNull(result.activeRide)
            assertNotNull(result.walletInfo)
        }

    @Test
    fun `wallet call fails returns active ride with null wallet`() =
        runTest {
            val rides = mockk<RidesRepository>()
            val wallet = mockk<WalletRepository>()
            coEvery { rides.getMyRides() } returns Result.success(listOf(acceptedRide))
            coEvery { wallet.getWallet() } returns Result.failure(RuntimeException("timeout"))

            val result = ResyncOnReconnectUseCase(rides, wallet)()

            assertNotNull(result.activeRide)
            assertNull(result.walletInfo)
        }

    @Test
    fun `both calls fail returns both null`() =
        runTest {
            val rides = mockk<RidesRepository>()
            val wallet = mockk<WalletRepository>()
            coEvery { rides.getMyRides() } returns Result.failure(RuntimeException("offline"))
            coEvery { wallet.getWallet() } returns Result.failure(RuntimeException("offline"))

            val result = ResyncOnReconnectUseCase(rides, wallet)()

            assertNull(result.activeRide)
            assertNull(result.walletInfo)
        }
}
