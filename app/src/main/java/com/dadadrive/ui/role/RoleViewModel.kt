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
import java.util.concurrent.atomic.AtomicBoolean
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

    private val selectRoleInFlight = AtomicBoolean(false)

    fun selectRole(role: String) {
        if (!selectRoleInFlight.compareAndSet(false, true)) return

        viewModelScope.launch {
            try {
                _state.value = RoleState.Loading

                if (tokenManager.getAccessToken().isNullOrBlank()) {
                    _state.value = RoleState.Error("Session expirée. Reconnectez-vous.")
                    return@launch
                }

                val localRole = userManager.getUser()?.role
                if (localRole == role && role != "pending") {
                    _state.value = RoleState.Success
                    return@launch
                }

                try {
                    authApiService.updateRole(UpdateRoleRequest(role))
                    userManager.updateRole(role)
                    _state.value = RoleState.Success
                } catch (e: HttpException) {
                    val body = e.response()?.errorBody()?.string().orEmpty()
                    if (e.code() == 400 && body.contains("already been set", ignoreCase = true)) {
                        userManager.updateRole(role)
                        _state.value = RoleState.Success
                        return@launch
                    }
                    val message = when (e.code()) {
                        400 -> "Rôle invalide."
                        401 -> "Session expirée. Reconnectez-vous."
                        else -> "Erreur serveur (${e.code()})."
                    }
                    _state.value = RoleState.Error(message)
                } catch (e: Exception) {
                    userManager.updateRole(role)
                    _state.value = RoleState.Success
                }
            } finally {
                selectRoleInFlight.set(false)
            }
        }
    }
}
