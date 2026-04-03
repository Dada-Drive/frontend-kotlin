// Équivalent Swift : Presentation/AppCoordinatorViewModel.swift (+ effet TokenStore sur erreur getMe)
package com.dadadrive.ui.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.data.local.TokenManager
import com.dadadrive.data.local.UserManager
import com.dadadrive.data.remote.AuthNavigationEvents
import com.dadadrive.data.remote.api.AuthApiService
import com.dadadrive.data.remote.model.toDomainUser
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
    private val authApiService: AuthApiService,
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
                _sessionState.value = SessionUiState.Unauthenticated
                return@launch
            }
            try {
                val me = authApiService.getMe()
                val dto = me.user
                val user = dto.toDomainUser()
                userManager.saveUser(user)
                _sessionState.value = nextStateAfterMe(dto.phone, dto.fullName, dto.role, user)
            } catch (e: Exception) {
                Log.w("Session", "getMe failed: ${e.message}")
                tokenManager.clearTokens()
                userManager.clearUser()
                _sessionState.value = SessionUiState.Unauthenticated
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

    fun forceLogout() {
        viewModelScope.launch {
            runCatching { authRepository.logout() }
            _sessionState.value = SessionUiState.Unauthenticated
        }
    }

    private fun <T> Result<T>.isHttpNotFound(): Boolean {
        val e = exceptionOrNull() ?: return false
        return e is HttpException && e.code() == 404
    }
}
