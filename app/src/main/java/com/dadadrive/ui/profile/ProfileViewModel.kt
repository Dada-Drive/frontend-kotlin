package com.dadadrive.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.domain.model.User
import com.dadadrive.domain.usecase.auth.LogoutUseCase
import com.dadadrive.domain.usecase.user.GetUserProfileUseCase
import com.dadadrive.domain.usecase.user.UpdateUserProfileUseCase
import com.dadadrive.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLoggedOut: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getUserProfileUseCase(userId)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, user = result.data) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = updateUserProfileUseCase(user)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSaving = false, user = result.data, successMessage = "Profil mis à jour")
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            when (logoutUseCase()) {
                is Resource.Success -> _uiState.update { it.copy(isLoggedOut = true) }
                is Resource.Error -> _uiState.update { it.copy(errorMessage = "Erreur lors de la déconnexion") }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
