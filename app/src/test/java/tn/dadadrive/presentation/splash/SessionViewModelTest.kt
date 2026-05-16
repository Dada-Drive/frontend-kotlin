package tn.dadadrive.presentation.splash

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import tn.dadadrive.data.local.SessionProfileCache
import tn.dadadrive.data.network.AuthNavigationEvents
import tn.dadadrive.data.storage.TokenManager
import tn.dadadrive.data.storage.UserManager
import tn.dadadrive.domain.models.User
import tn.dadadrive.domain.protocols.AuthRepository
import tn.dadadrive.domain.protocols.DriverRepository
import tn.dadadrive.domain.protocols.WalletRepository
import tn.dadadrive.presentation.notifications.PushTokenRegistrar
import tn.dadadrive.presentation.session.SessionState

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    @Test
    fun noTokenUnauthenticated() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val tokenManager = mockk<TokenManager>(relaxed = true)
            coEvery { tokenManager.getAccessToken() } returns null
            coEvery { tokenManager.isGuestBrowseEnabled() } returns false

            val vm = createViewModel(tokenManager = tokenManager)

            assertEquals(SessionState.Unauthenticated, vm.sessionState.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun guestBrowseWithoutToken() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val tokenManager = mockk<TokenManager>(relaxed = true)
            coEvery { tokenManager.getAccessToken() } returns null
            coEvery { tokenManager.isGuestBrowseEnabled() } returns true

            val vm = createViewModel(tokenManager = tokenManager)

            assertEquals(SessionState.BrowsingGuest, vm.sessionState.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun getMeSuccessRiderAuthenticated() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val tokenManager = mockk<TokenManager>(relaxed = true)
            coEvery { tokenManager.getAccessToken() } returns "tok"
            val user = User(
                id = "1",
                fullName = "Name",
                email = "e@e.com",
                phoneNumber = "+2161",
                role = "rider",
                profilePictureUri = null,
            )
            val authRepository: AuthRepository = StubAuthRepository { Result.success(user) }

            val vm = createViewModel(tokenManager = tokenManager, authRepository = authRepository)

            assertEquals(SessionState.Authenticated(user), vm.sessionState.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun getMe401ClearsSession() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val tokenManager = mockk<TokenManager>(relaxed = true)
            coEvery { tokenManager.getAccessToken() } returns "tok"
            val err = HttpException(Response.error<Unit>(401, "".toResponseBody("text/plain".toMediaType())))
            val authRepository: AuthRepository = StubAuthRepository { Result.failure(err) }
            val userManager = mockk<UserManager>(relaxed = true)
            val sessionProfileCache = mockk<SessionProfileCache>(relaxed = true)

            val vm = createViewModel(
                tokenManager = tokenManager,
                authRepository = authRepository,
                userManager = userManager,
                sessionProfileCache = sessionProfileCache,
            )

            verify { tokenManager.clearTokens() }
            verify { userManager.clearUser() }
            coVerify { sessionProfileCache.clear() }
            assertEquals(SessionState.Unauthenticated, vm.sessionState.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun getMeTimeoutUsesRoomCache() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        try {
            val tokenManager = mockk<TokenManager>(relaxed = true)
            coEvery { tokenManager.getAccessToken() } returns "tok"
            val authRepository: AuthRepository = StubAuthRepository {
                awaitCancellation()
            }
            val cached = User("1", "N", "", "+216", "rider", null)
            val sessionProfileCache = mockk<SessionProfileCache>(relaxed = true)
            coEvery { sessionProfileCache.loadUser() } returns cached
            val userManager = mockk<UserManager>(relaxed = true)
            coEvery { userManager.getUser() } returns null

            val vm = createViewModel(
                tokenManager = tokenManager,
                authRepository = authRepository,
                sessionProfileCache = sessionProfileCache,
                userManager = userManager,
            )
            advanceUntilIdle()
            advanceTimeBy(5_001)
            advanceUntilIdle()

            val state = vm.sessionState.value
            assertTrue(state is SessionState.Authenticated && (state as SessionState.Authenticated).user == cached)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun getMeNetworkErrorUsesCachedProfile() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val tokenManager = mockk<TokenManager>(relaxed = true)
            coEvery { tokenManager.getAccessToken() } returns "tok"
            val cached = User("1", "N", "", "+216", "rider", null)
            val authRepository: AuthRepository = StubAuthRepository {
                Result.failure(Exception("network"))
            }
            val sessionProfileCache = mockk<SessionProfileCache>(relaxed = true)
            coEvery { sessionProfileCache.loadUser() } returns cached
            val userManager = mockk<UserManager>(relaxed = true)
            coEvery { userManager.getUser() } returns null

            val vm = createViewModel(
                tokenManager = tokenManager,
                authRepository = authRepository,
                sessionProfileCache = sessionProfileCache,
                userManager = userManager,
            )

            assertEquals(SessionState.Authenticated(cached), vm.sessionState.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun createViewModel(
        tokenManager: TokenManager = mockk(relaxed = true),
        userManager: UserManager = mockk(relaxed = true),
        driverRepository: DriverRepository = mockk(relaxed = true),
        authRepository: AuthRepository = mockk(relaxed = true),
        authNavigationEvents: AuthNavigationEvents = AuthNavigationEvents(),
        pushTokenRegistrar: PushTokenRegistrar = mockk(relaxed = true),
        sessionProfileCache: SessionProfileCache = mockk(relaxed = true),
        walletRepository: WalletRepository = mockk(relaxed = true),
    ) = SessionViewModel(
        tokenManager = tokenManager,
        userManager = userManager,
        driverRepository = driverRepository,
        authRepository = authRepository,
        authNavigationEvents = authNavigationEvents,
        pushTokenRegistrar = pushTokenRegistrar,
        sessionProfileCache = sessionProfileCache,
        walletRepository = walletRepository,
    )
}
