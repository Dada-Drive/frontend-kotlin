// Équivalent Swift : Presentation/AppCoordinatorViewModel.swift (+ effet TokenStore sur erreur getMe)
package tn.dadadrive.presentation.splash

import tn.dadadrive.core.logging.AppLogger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tn.dadadrive.core.debug.DebugAuthConfig
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val driverRepository: DriverRepository,
    private val authRepository: AuthRepository,
    private val authNavigationEvents: AuthNavigationEvents,
    private val pushTokenRegistrar: PushTokenRegistrar,
    private val sessionProfileCache: SessionProfileCache,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    /** When true, [SessionNavigationMapper] sends the user to the passenger map while driver approval is still pending. */
    private val _preferPassengerMapWhileDriverPending = MutableStateFlow(false)
    val preferPassengerMapWhileDriverPending: StateFlow<Boolean> = _preferPassengerMapWhileDriverPending.asStateFlow()

    fun preferPassengerBrowseWhileDriverPending() {
        _preferPassengerMapWhileDriverPending.value = true
    }

    private fun clearPassengerBypassUnlessStillPending(next: SessionState) {
        if (next != SessionState.NeedsDriverApproval) {
            _preferPassengerMapWhileDriverPending.value = false
        }
    }

    init {
        viewModelScope.launch {
            authNavigationEvents.forceLogout.collect {
                tokenManager.clearTokens()
                tokenManager.setGuestBrowseEnabled(false)
                userManager.clearUser()
                sessionProfileCache.clear()
                _preferPassengerMapWhileDriverPending.value = false
                _sessionState.value = SessionState.Unauthenticated
            }
        }
        restoreSession()
    }

    fun restoreSession() {
        viewModelScope.launch { restoreSessionBody(showLoading = true) }
    }

    fun refreshSession() = restoreSession()

    fun onAppForegrounded() {
        viewModelScope.launch {
            runCatching { walletRepository.getWallet() }
            silentForegroundRefresh()
        }
    }

    private suspend fun restoreSessionBody(showLoading: Boolean) {
        if (showLoading) {
            _sessionState.value = SessionState.Loading
        }
        val token = tokenManager.getAccessToken()
        if (token.isNullOrBlank()) {
            _preferPassengerMapWhileDriverPending.value = false
            _sessionState.value = if (tokenManager.isGuestBrowseEnabled()) {
                SessionState.BrowsingGuest
            } else {
                SessionState.Unauthenticated
            }
            return
        }
        val result = withTimeoutOrNull(5_000) { authRepository.getCurrentUser() }
        if (result == null) {
            AppLogger.w("Session: getMe timeout (5s) — fallback cache / prefs")
            val cached = sessionProfileCache.loadUser() ?: userManager.getUser()
            val next = cached?.let { offlineStateFromUser(it) } ?: SessionState.Unauthenticated
            clearPassengerBypassUnlessStillPending(next)
            _sessionState.value = next
            return
        }
        result.fold(
            onSuccess = { user ->
                pushTokenRegistrar.registerCurrentToken(viewModelScope)
                val next = nextStateAfterMe(
                    user.phoneNumber,
                    user.fullName,
                    user.role,
                    user,
                )
                clearPassengerBypassUnlessStillPending(next)
                _sessionState.value = next
            },
            onFailure = { e ->
                if (e is HttpException && (e.code() == 401 || e.code() == 403)) {
                    AppLogger.w("Session: getMe auth failed: code=${e.code()}")
                    tokenManager.clearTokens()
                    userManager.clearUser()
                    sessionProfileCache.clear()
                    _preferPassengerMapWhileDriverPending.value = false
                    _sessionState.value = SessionState.Unauthenticated
                } else {
                    AppLogger.w("Session: getMe failed: ${e.message}")
                    val cached = sessionProfileCache.loadUser() ?: userManager.getUser()
                    val next = cached?.let { offlineStateFromUser(it) }
                        ?: SessionState.Unauthenticated
                    clearPassengerBypassUnlessStillPending(next)
                    _sessionState.value = next
                }
            },
        )
    }

    private suspend fun silentForegroundRefresh() {
        val token = tokenManager.getAccessToken()
        if (token.isNullOrBlank()) return
        val result = withTimeoutOrNull(5_000) { authRepository.getCurrentUser() }
        if (result == null) return
        result.fold(
            onSuccess = { user ->
                pushTokenRegistrar.registerCurrentToken(viewModelScope)
                val next = nextStateAfterMe(user.phoneNumber, user.fullName, user.role, user)
                clearPassengerBypassUnlessStillPending(next)
                if (next != _sessionState.value) {
                    _sessionState.value = next
                }
            },
            onFailure = { e ->
                if (e is HttpException && (e.code() == 401 || e.code() == 403)) {
                    tokenManager.clearTokens()
                    userManager.clearUser()
                    sessionProfileCache.clear()
                    _preferPassengerMapWhileDriverPending.value = false
                    _sessionState.value = SessionState.Unauthenticated
                }
            },
        )
    }

    private fun offlineStateFromUser(user: User): SessionState {
        if (user.phoneNumber.isBlank()) return SessionState.NeedsPhone
        if (user.fullName.isBlank()) return SessionState.NeedsName
        val role = user.role.ifBlank { "rider" }
        if (role == "pending") return SessionState.NeedsRole
        return SessionState.Authenticated(user)
    }

    private suspend fun nextStateAfterMe(
        phone: String?,
        fullName: String?,
        roleRaw: String,
        user: User,
    ): SessionState {
        if (phone.isNullOrBlank()) return SessionState.NeedsPhone
        if (fullName.isNullOrBlank()) return SessionState.NeedsName
        val role = roleRaw.ifBlank { "rider" }
        if (role == "pending") return SessionState.NeedsRole
        if (role == "driver") {
            val profileResult = driverRepository.getProfile()
            if (profileResult.isSuccess) {
                val p = profileResult.getOrThrow()
                if (p.isApproved) {
                    return SessionState.Authenticated(user)
                }
                val vehicleResult = driverRepository.getVehicle()
                val needsVehicle = vehicleResult.isHttpNotFound() || vehicleResult.isFailure
                if (needsVehicle) {
                    return SessionState.NeedsDriverSetup
                }
                return SessionState.NeedsDriverApproval
            }
            return SessionState.NeedsDriverSetup
        }
        return SessionState.Authenticated(user)
    }

    fun continueWithoutAccount() {
        tokenManager.setGuestBrowseEnabled(true)
        tokenManager.clearTokens()
        if (DebugAuthConfig.shouldInjectStaticUserOnWelcomeSkip()) {
            userManager.saveUser(DebugAuthConfig.staticSkipUser)
        } else {
            userManager.clearUser()
        }
        _sessionState.value = SessionState.BrowsingGuest
        _preferPassengerMapWhileDriverPending.value = false
    }

    fun forceLogout() {
        viewModelScope.launch {
            tokenManager.setGuestBrowseEnabled(false)
            withTimeoutOrNull(5_000) {
                runCatching { pushTokenRegistrar.unregisterToken() }
                runCatching { authRepository.logout() }
            }
            tokenManager.clearTokens()
            userManager.clearUser()
            sessionProfileCache.clear()
            _preferPassengerMapWhileDriverPending.value = false
            _sessionState.value = SessionState.Unauthenticated
        }
    }

    private fun <T> Result<T>.isHttpNotFound(): Boolean {
        val e = exceptionOrNull() ?: return false
        return e is HttpException && e.code() == 404
    }
}
