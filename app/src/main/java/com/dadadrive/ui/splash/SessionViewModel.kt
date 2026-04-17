// Équivalent Swift : Presentation/AppCoordinatorViewModel.swift (+ effet TokenStore sur erreur getMe)
package com.dadadrive.ui.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.core.debug.DebugAuthConfig
import com.dadadrive.data.local.TokenManager
import com.dadadrive.data.local.UserManager
import com.dadadrive.data.remote.AuthNavigationEvents
import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository
import com.dadadrive.domain.repository.DriverRepository
import com.dadadrive.ui.session.SessionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val driverRepository: DriverRepository,
    private val authRepository: AuthRepository,
    private val authNavigationEvents: AuthNavigationEvents
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
    val sessionState: StateFlow<SessionUiState> = _sessionState.asStateFlow()

    init {
        viewModelScope.launch {
            authNavigationEvents.forceLogout.collect {
                tokenManager.clearTokens()
                tokenManager.setGuestBrowseEnabled(false)
                userManager.clearUser()
                _sessionState.value = SessionUiState.Unauthenticated
            }
        }
        refreshSession()
    }

    fun refreshSession() {
        viewModelScope.launch {
            _sessionState.value = SessionUiState.Loading
            val token = tokenManager.getAccessToken()
            if (token.isNullOrBlank()) {
                _sessionState.value = if (tokenManager.isGuestBrowseEnabled()) {
                    SessionUiState.BrowsingGuest
                } else {
                    SessionUiState.Unauthenticated
                }
                return@launch
            }
            try {
                val currentUserResult = authRepository.getCurrentUser()
                val user = currentUserResult.getOrThrow()
                _sessionState.value = nextStateAfterMe(
                    user.phoneNumber,
                    user.fullName,
                    user.role,
                    user
                )
            } catch (e: HttpException) {
                Log.w("Session", "getMe http failed: code=${e.code()} msg=${e.message()}")
                if (e.code() == 401 || e.code() == 403) {
                    tokenManager.clearTokens()
                    userManager.clearUser()
                    _sessionState.value = SessionUiState.Unauthenticated
                } else {
                    val cached = userManager.getUser()
                    _sessionState.value = cached?.let { SessionUiState.Authenticated(it) }
                        ?: SessionUiState.Unauthenticated
                }
            } catch (e: Exception) {
                Log.w("Session", "getMe network/unknown failed: ${e.message}")
                val cached = userManager.getUser()
                _sessionState.value = cached?.let { SessionUiState.Authenticated(it) }
                    ?: SessionUiState.Unauthenticated
            }
        }
    }

    private suspend fun nextStateAfterMe(
        phone: String?,
        fullName: String?,
        roleRaw: String,
        user: User
    ): SessionUiState {
        if (phone.isNullOrBlank()) return SessionUiState.NeedsPhone
        if (fullName.isNullOrBlank()) return SessionUiState.NeedsName
        val role = roleRaw.ifBlank { "rider" }
        if (role == "pending") return SessionUiState.NeedsRole
        if (role == "driver") {
            val profile = driverRepository.getProfile()
            val vehicle = driverRepository.getVehicle()
            // 404 = profil / véhicule pas encore créés → onboarding driver (pas une erreur fatale).
            val onboardingIncomplete = profile.isHttpNotFound() || vehicle.isHttpNotFound() ||
                profile.isFailure || vehicle.isFailure
            if (onboardingIncomplete) {
                return SessionUiState.NeedsDriverSetup
            }
        }
        return SessionUiState.Authenticated(user)
    }

    /**
     * Entrée immédiate sur la carte en invité (sans attendre le réseau).
     * Mise à jour synchrone de l’état pour que la navigation parte tout de suite ;
     * la déconnexion serveur éventuelle est lancée en arrière-plan.
     */
    fun continueWithoutAccount() {
        tokenManager.setGuestBrowseEnabled(true)
        tokenManager.clearTokens()
        if (DebugAuthConfig.shouldInjectStaticUserOnWelcomeSkip()) {
            userManager.saveUser(DebugAuthConfig.staticSkipUser)
        } else {
            userManager.clearUser()
        }
        _sessionState.value = SessionUiState.BrowsingGuest
        // Pas d’appel logout() ici : sans refresh, l’API peut répondre erreur et effacer user local.
    }

    fun forceLogout() {
        viewModelScope.launch {
            tokenManager.setGuestBrowseEnabled(false)
            runCatching { authRepository.logout() }
            tokenManager.clearTokens()
            userManager.clearUser()
            _sessionState.value = SessionUiState.Unauthenticated
        }
    }

    private fun <T> Result<T>.isHttpNotFound(): Boolean {
        val e = exceptionOrNull() ?: return false
        return e is HttpException && e.code() == 404
    }
}
