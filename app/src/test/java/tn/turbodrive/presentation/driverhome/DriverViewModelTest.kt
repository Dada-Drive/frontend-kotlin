package tn.turbodrive.presentation.driverhome

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import tn.turbodrive.data.local.ActiveRideDraftCache
import tn.turbodrive.data.network.error.PresentableErrorMapper
import tn.turbodrive.domain.models.ActiveRide
import tn.turbodrive.domain.models.AvailableRide
import tn.turbodrive.domain.models.DriverProfile
import tn.turbodrive.domain.models.ErrorCategory
import tn.turbodrive.domain.models.PresentableError
import tn.turbodrive.domain.models.RideStatus
import tn.turbodrive.domain.usecases.driver.AcceptRideUseCase
import tn.turbodrive.domain.usecases.driver.CancelRideUseCase
import tn.turbodrive.domain.usecases.driver.CompleteRideUseCase
import tn.turbodrive.domain.usecases.driver.GetAvailableRidesUseCase
import tn.turbodrive.domain.usecases.driver.GetMyRidesUseCase
import tn.turbodrive.domain.usecases.driver.RefuseRideUseCase
import tn.turbodrive.domain.usecases.driver.SetOnlineStatusUseCase
import tn.turbodrive.domain.usecases.driver.StartRideUseCase
import tn.turbodrive.presentation.common.ScreenState

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DriverViewModelTest {
    private val onlineProfile =
        DriverProfile(
            id = "p1",
            userId = "u1",
            licenseNumber = "TN-1",
            licenseExpiry = "2030-01-01",
            isApproved = true,
            isOnline = true,
            rating = null,
            totalRides = 0,
        )

    private val offlineProfile = onlineProfile.copy(isOnline = false)

    private val sampleRide =
        ActiveRide(
            id = "r1",
            riderName = "Alice",
            riderPhone = "+216",
            pickupAddress = "A",
            dropoffAddress = "B",
            pickupLat = 36.81,
            pickupLng = 10.18,
            dropoffLat = 36.82,
            dropoffLng = 10.19,
            distanceKm = 1.0,
            estimatedMinutes = 5,
            finalFare = null,
            calculatedFare = 5.0,
            status = RideStatus.Accepted,
            startedAt = null,
            completedAt = null,
        )

    private val mappedError =
        PresentableError(
            message = "mapped failure",
            category = ErrorCategory.Server,
            isRetryable = true,
        )

    @Test
    fun initialStateIsLoadedFalseAndIdleRide() =
        runTest {
            val vm = createViewModel()
            assertEquals(ScreenState.Loaded(false), vm.onlineState.value)
            assertEquals(ScreenState.Loaded(emptyList<AvailableRide>()), vm.availableRidesState.value)
            assertEquals(ScreenState.Idle, vm.activeRideState.value)
        }

    @Test
    fun toggleOnlineSuccessEmitsLoadingThenLoaded() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            try {
                val setOnline = mockk<SetOnlineStatusUseCase>()
                val availableRides = mockk<GetAvailableRidesUseCase>()
                val myRides = mockk<GetMyRidesUseCase>()
                coEvery { setOnline.invoke(true) } returns Result.success(onlineProfile)
                coEvery { availableRides.invoke() } returns Result.success(emptyList())
                coEvery { myRides.invoke() } returns Result.success(emptyList())

                val vm =
                    createViewModel(
                        setOnlineStatusUseCase = setOnline,
                        getAvailableRidesUseCase = availableRides,
                        getMyRidesUseCase = myRides,
                    )

                vm.toggleOnlineStatus()

                assertEquals(ScreenState.Loaded(true), vm.onlineState.value)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun toggleOnlineFailureEmitsError() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            try {
                val boom = IllegalStateException("network down")
                val setOnline = mockk<SetOnlineStatusUseCase>()
                coEvery { setOnline.invoke(any()) } returns Result.failure(boom)
                val mapper = mockk<PresentableErrorMapper>()
                every { mapper.fromThrowable(boom) } returns mappedError

                val vm =
                    createViewModel(
                        setOnlineStatusUseCase = setOnline,
                        errorMapper = mapper,
                    )

                vm.toggleOnlineStatus()

                val state = vm.onlineState.value
                assertTrue("onlineState must be Error after toggle failure", state is ScreenState.Error)
                assertSame(mappedError, (state as ScreenState.Error).error)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun toggleOnlineToOfflineClearsAvailableAndActive() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            try {
                val setOnline = mockk<SetOnlineStatusUseCase>()
                val availableRides = mockk<GetAvailableRidesUseCase>()
                val myRides = mockk<GetMyRidesUseCase>()
                // First call: go online (returns isOnline=true). Then a second call: go offline (returns isOnline=false).
                coEvery { setOnline.invoke(true) } returns Result.success(onlineProfile)
                coEvery { setOnline.invoke(false) } returns Result.success(offlineProfile)
                coEvery { availableRides.invoke() } returns Result.success(emptyList())
                coEvery { myRides.invoke() } returns Result.success(emptyList())

                val vm =
                    createViewModel(
                        setOnlineStatusUseCase = setOnline,
                        getAvailableRidesUseCase = availableRides,
                        getMyRidesUseCase = myRides,
                    )

                vm.toggleOnlineStatus() // online
                assertEquals(ScreenState.Loaded(true), vm.onlineState.value)
                vm.toggleOnlineStatus() // offline
                assertEquals(ScreenState.Loaded(false), vm.onlineState.value)
                assertEquals(ScreenState.Loaded(emptyList<AvailableRide>()), vm.availableRidesState.value)
                assertEquals(ScreenState.Idle, vm.activeRideState.value)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun startRideFailureEmitsErrorOnActiveRideStateOnly() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            try {
                val setOnline = mockk<SetOnlineStatusUseCase>()
                val availableRides = mockk<GetAvailableRidesUseCase>()
                val myRides = mockk<GetMyRidesUseCase>()
                val startRide = mockk<StartRideUseCase>()
                val boom = IllegalStateException("cannot start")

                coEvery { setOnline.invoke(true) } returns Result.success(onlineProfile)
                coEvery { availableRides.invoke() } returns Result.success(emptyList())
                coEvery { myRides.invoke() } returns Result.success(listOf(sampleRide))
                coEvery { startRide.invoke(sampleRide.id) } returns Result.failure(boom)
                val mapper = mockk<PresentableErrorMapper>()
                every { mapper.fromThrowable(boom) } returns mappedError

                val vm =
                    createViewModel(
                        setOnlineStatusUseCase = setOnline,
                        getAvailableRidesUseCase = availableRides,
                        getMyRidesUseCase = myRides,
                        startRideUseCase = startRide,
                        errorMapper = mapper,
                    )
                vm.toggleOnlineStatus() // online + fetchMyRides loads sampleRide
                assertEquals(ScreenState.Loaded(sampleRide), vm.activeRideState.value)

                vm.startRide()

                val active = vm.activeRideState.value
                assertTrue(active is ScreenState.Error)
                // online & availableRides untouched -- per-domain error containment.
                assertEquals(ScreenState.Loaded(true), vm.onlineState.value)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Suppress("LongParameterList")
    private fun createViewModel(
        setOnlineStatusUseCase: SetOnlineStatusUseCase = mockk(relaxed = true),
        getAvailableRidesUseCase: GetAvailableRidesUseCase = mockk(relaxed = true),
        acceptRideUseCase: AcceptRideUseCase = mockk(relaxed = true),
        refuseRideUseCase: RefuseRideUseCase = mockk(relaxed = true),
        getMyRidesUseCase: GetMyRidesUseCase = mockk(relaxed = true),
        startRideUseCase: StartRideUseCase = mockk(relaxed = true),
        completeRideUseCase: CompleteRideUseCase = mockk(relaxed = true),
        cancelRideUseCase: CancelRideUseCase = mockk(relaxed = true),
        activeRideDraftCache: ActiveRideDraftCache = mockk(relaxed = true),
        errorMapper: PresentableErrorMapper = mockk(relaxed = true),
    ) = DriverViewModel(
        setOnlineStatusUseCase = setOnlineStatusUseCase,
        getAvailableRidesUseCase = getAvailableRidesUseCase,
        acceptRideUseCase = acceptRideUseCase,
        refuseRideUseCase = refuseRideUseCase,
        getMyRidesUseCase = getMyRidesUseCase,
        startRideUseCase = startRideUseCase,
        completeRideUseCase = completeRideUseCase,
        cancelRideUseCase = cancelRideUseCase,
        activeRideDraftCache = activeRideDraftCache,
        errorMapper = errorMapper,
    )
}
