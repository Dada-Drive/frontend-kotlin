package com.dadadrive.ui.role

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.data.local.TokenManager
import com.dadadrive.data.local.UserManager
import com.dadadrive.data.remote.api.AuthApiService
import com.dadadrive.data.remote.model.UpdateRoleRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class RoleViewModel @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager,
    private val userManager: UserManager
) : ViewModel() {

    sealed class RoleState {
        object Idle : RoleState()
        object Loading : RoleState()
        object Success : RoleState()
        data class Error(val message: String) : RoleState()
    }

    private val _state = MutableStateFlow<RoleState>(RoleState.Idle)
    val state: StateFlow<RoleState> = _state.asStateFlow()

    fun selectRole(role: String) {
        viewModelScope.launch {
            _state.value = RoleState.Loading

            if (tokenManager.getAccessToken().isNullOrBlank()) {
                _state.value = RoleState.Error("Session expirée. Reconnectez-vous.")
                return@launch
            }

            try {
                // AuthInterceptor ajoute le token automatiquement
                authApiService.updateRole(UpdateRoleRequest(role))
                // Sauvegarder le rôle localement après succès API
                userManager.updateRole(role)
                _state.value = RoleState.Success
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    400 -> "Rôle invalide."
                    401 -> "Session expirée. Reconnectez-vous."
                    else -> "Erreur serveur (${e.code()})."
                }
                // En cas d'erreur réseau, sauvegarder localement quand même
                userManager.updateRole(role)
                _state.value = RoleState.Error(message)
            } catch (e: Exception) {
                // Pas de connexion : sauvegarder localement et continuer
                userManager.updateRole(role)
                _state.value = RoleState.Success
            }
        }
    }
}
