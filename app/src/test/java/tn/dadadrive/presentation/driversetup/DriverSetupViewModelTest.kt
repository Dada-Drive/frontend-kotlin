package tn.dadadrive.presentation.driversetup

import android.content.Context
import com.dadadrive.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tn.dadadrive.data.network.error.PresentableErrorMapper
import tn.dadadrive.domain.models.DriverProfile
import tn.dadadrive.domain.models.ErrorCategory
import tn.dadadrive.domain.models.PresentableError
import tn.dadadrive.domain.models.Vehicle
import tn.dadadrive.domain.usecases.driver.CreateDriverProfileUseCase
import tn.dadadrive.domain.usecases.driver.CreateVehicleUseCase
import tn.dadadrive.presentation.common.ScreenState

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DriverSetupViewModelTest {
    private val sampleProfile =
        DriverProfile(
            id = "p1",
            userId = "u1",
            licenseNumber = "TN-1",
            licenseExpiry = "2030-01-01",
            isApproved = false,
            isOnline = false,
            rating = null,
            totalRides = 0,
        )

    private val sampleVehicle =
        Vehicle(
            id = "v1",
            make = "Toyota",
            model = "Yaris",
            year = 2020,
            plateNumber = "123 TUN 4567",
            color = "white",
            vehicleType = "citadine",
        )

    private val validationError =
        PresentableError(
            message = "validation failed",
            category = ErrorCategory.Validation,
            isRetryable = false,
        )

    @Test
    fun initialStateIsIdle() =
        runTest {
            val vm = createViewModel()
            assertEquals(ScreenState.Idle, vm.state.value)
        }

    @Test
    fun submitSuccessEmitsLoadedAndInvokesOnComplete() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            try {
                val profileUseCase = mockk<CreateDriverProfileUseCase>()
                val vehicleUseCase = mockk<CreateVehicleUseCase>()
                coEvery {
                    profileUseCase(any(), any(), any(), any(), any(), any(), any(), any())
                } returns Result.success(sampleProfile)
                coEvery {
                    vehicleUseCase(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
                } returns Result.success(sampleVehicle)

                val vm =
                    createViewModel(
                        profileUseCase = profileUseCase,
                        vehicleUseCase = vehicleUseCase,
                    )
                var completeCalled = false

                vm.submit { completeCalled = true }

                assertEquals(ScreenState.Loaded(Unit), vm.state.value)
                assertTrue("onComplete must be invoked on success", completeCalled)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun submitVehicleFailureEmitsError() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            try {
                val profileUseCase = mockk<CreateDriverProfileUseCase>()
                val vehicleUseCase = mockk<CreateVehicleUseCase>()
                val boom = IllegalStateException("boom")
                coEvery {
                    profileUseCase(any(), any(), any(), any(), any(), any(), any(), any())
                } returns Result.success(sampleProfile)
                coEvery {
                    vehicleUseCase(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
                } returns Result.failure(boom)
                val mapper = mockk<PresentableErrorMapper>()
                every { mapper.fromThrowable(boom) } returns validationError

                val vm =
                    createViewModel(
                        profileUseCase = profileUseCase,
                        vehicleUseCase = vehicleUseCase,
                        mapper = mapper,
                    )
                var completeCalled = false
                vm.submit { completeCalled = true }

                val finalState = vm.state.value
                assertTrue("state must be Error", finalState is ScreenState.Error)
                // profileCreateFailed = false (profile succeeded) => no override
                assertEquals(validationError, (finalState as ScreenState.Error).error)
                assertEquals(false, completeCalled)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun bothProfileAndVehicleFailValidationOverridesMessage() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            try {
                val profileUseCase = mockk<CreateDriverProfileUseCase>()
                val vehicleUseCase = mockk<CreateVehicleUseCase>()
                val profileEx = IllegalStateException("profile-exists")
                val vehicleEx = IllegalStateException("vehicle-bad")
                coEvery {
                    profileUseCase(any(), any(), any(), any(), any(), any(), any(), any())
                } returns Result.failure(profileEx)
                coEvery {
                    vehicleUseCase(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
                } returns Result.failure(vehicleEx)
                val mapper = mockk<PresentableErrorMapper>()
                every { mapper.fromThrowable(vehicleEx) } returns validationError

                val context = mockk<Context>()
                every { context.getString(R.string.driver_setup_error_profile_vehicle) } returns "combined-message"

                val vm =
                    createViewModel(
                        appContext = context,
                        profileUseCase = profileUseCase,
                        vehicleUseCase = vehicleUseCase,
                        mapper = mapper,
                    )
                vm.submit { /* onComplete must NOT be called */ }

                val finalState = vm.state.value
                assertTrue(finalState is ScreenState.Error)
                val err = (finalState as ScreenState.Error).error
                assertEquals("combined-message", err.message)
                coVerify(exactly = 1) { mapper.fromThrowable(vehicleEx) }
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun dismissErrorResetsToIdleOnlyFromErrorState() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            try {
                val profileUseCase = mockk<CreateDriverProfileUseCase>()
                val vehicleUseCase = mockk<CreateVehicleUseCase>()
                val boom = IllegalStateException("boom")
                coEvery {
                    profileUseCase(any(), any(), any(), any(), any(), any(), any(), any())
                } returns Result.success(sampleProfile)
                coEvery {
                    vehicleUseCase(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
                } returns Result.failure(boom)
                val mapper = mockk<PresentableErrorMapper>()
                every { mapper.fromThrowable(boom) } returns validationError

                val vm =
                    createViewModel(
                        profileUseCase = profileUseCase,
                        vehicleUseCase = vehicleUseCase,
                        mapper = mapper,
                    )
                vm.submit { }
                assertTrue(vm.state.value is ScreenState.Error)

                vm.dismissError()
                assertEquals(ScreenState.Idle, vm.state.value)

                // From Idle, dismissError is a no-op.
                vm.dismissError()
                assertEquals(ScreenState.Idle, vm.state.value)
            } finally {
                Dispatchers.resetMain()
            }
        }

    private fun DriverSetupViewModel.submit(onComplete: () -> Unit) =
        submitDriverSetup(
            fullLicenseNumber = "TN-1",
            licenseExpiry = "2030-01-01",
            cin = "1234567",
            cinDeliveredAt = "2020-01-01",
            cinPhotoFront = "a",
            cinPhotoBack = "b",
            licensePhotoFront = "c",
            licensePhotoBack = "d",
            make = "Toyota",
            model = "Yaris",
            year = 2020,
            plateNumber = "123 TUN 4567",
            color = "white",
            vehicleType = "citadine",
            seats = 4,
            photoFront = "e",
            photoSide = "f",
            photoBack = "g",
            onComplete = onComplete,
        )

    private fun createViewModel(
        appContext: Context = mockk(relaxed = true),
        profileUseCase: CreateDriverProfileUseCase = mockk(relaxed = true),
        vehicleUseCase: CreateVehicleUseCase = mockk(relaxed = true),
        mapper: PresentableErrorMapper = mockk(relaxed = true),
    ) = DriverSetupViewModel(
        appContext = appContext,
        createDriverProfileUseCase = profileUseCase,
        createVehicleUseCase = vehicleUseCase,
        errorMapper = mapper,
    )
}
