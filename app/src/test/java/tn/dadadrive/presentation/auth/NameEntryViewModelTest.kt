package tn.dadadrive.presentation.auth

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
import tn.dadadrive.data.network.error.PresentableErrorMapper
import tn.dadadrive.domain.models.ErrorCategory
import tn.dadadrive.domain.models.PresentableError
import tn.dadadrive.domain.models.User
import tn.dadadrive.domain.protocols.UserRepository
import tn.dadadrive.presentation.common.ScreenState

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NameEntryViewModelTest {
    private val sampleUser =
        User(
            id = "u1",
            fullName = "Alice",
            email = "a@a.com",
            phoneNumber = "+216",
            role = "rider",
            profilePictureUri = null,
        )

    private val mappedError =
        PresentableError(
            message = "mapped failure",
            category = ErrorCategory.Server,
            isRetryable = true,
        )

    @Test
    fun initialStateIsIdle() =
        runTest {
            val vm = createViewModel()
            assertEquals(ScreenState.Idle, vm.state.value)
        }

    @Test
    fun blankNameEmitsValidationErrorWithoutCallingRepository() =
        runTest {
            val repo = mockk<UserRepository>()
            val vm = createViewModel(userRepository = repo)
            var successCalled = false
            vm.submitFullName(fullName = "   ") { successCalled = true }

            val state = vm.state.value
            assertTrue("blank input must yield Error state", state is ScreenState.Error)
            assertEquals(ErrorCategory.Validation, (state as ScreenState.Error).error.category)
            assertEquals(false, successCalled)
        }

    @Test
    fun submitSuccessEmitsLoadedAndInvokesCallback() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            try {
                val repo = mockk<UserRepository>()
                coEvery { repo.updateProfile(any(), any(), any()) } returns Result.success(sampleUser)
                val vm = createViewModel(userRepository = repo)
                var successCalled = false

                vm.submitFullName(fullName = "Bob") { successCalled = true }

                assertEquals(ScreenState.Loaded(Unit), vm.state.value)
                assertTrue("onSuccess must be invoked", successCalled)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun submitFailureEmitsErrorWithMappedThrowable() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            try {
                val repo = mockk<UserRepository>()
                val boom = IllegalStateException("boom")
                coEvery { repo.updateProfile(any(), any(), any()) } returns Result.failure(boom)
                val mapper = mockk<PresentableErrorMapper>()
                every { mapper.fromThrowable(boom) } returns mappedError

                val vm = createViewModel(userRepository = repo, errorMapper = mapper)

                vm.submitFullName(fullName = "Carol") { /* should not fire */ }

                val state = vm.state.value
                assertTrue(state is ScreenState.Error)
                assertSame(mappedError, (state as ScreenState.Error).error)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun dismissErrorResetsToIdleOnlyFromErrorState() =
        runTest {
            val repo = mockk<UserRepository>()
            val vm = createViewModel(userRepository = repo)
            vm.submitFullName("   ") { /* triggers blank-name Error */ }
            assertTrue(vm.state.value is ScreenState.Error)

            vm.dismissError()
            assertEquals(ScreenState.Idle, vm.state.value)

            // No-op from Idle.
            vm.dismissError()
            assertEquals(ScreenState.Idle, vm.state.value)
        }

    private fun createViewModel(
        userRepository: UserRepository = mockk(relaxed = true),
        errorMapper: PresentableErrorMapper = mockk(relaxed = true),
    ) = NameEntryViewModel(
        userRepository = userRepository,
        errorMapper = errorMapper,
    )
}
