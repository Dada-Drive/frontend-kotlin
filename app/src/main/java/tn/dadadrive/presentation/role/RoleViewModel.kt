package tn.dadadrive.presentation.role

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tn.dadadrive.data.storage.TokenManager
import tn.dadadrive.data.storage.UserManager
import tn.dadadrive.domain.protocols.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class RoleViewModel @Inject constructor(
    private val userRepository: UserRepository,
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

                userRepository.updateRole(role).fold(
                    onSuccess = { _state.value = RoleState.Success },
                    onFailure = { e ->
                        _state.value = RoleState.Error(e.message ?: "Erreur réseau.")
                    },
                )
            } finally {
                selectRoleInFlight.set(false)
            }
        }
    }
}
