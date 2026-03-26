package com.dadadrive.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.domain.usecase.LoginUseCase
import com.dadadrive.domain.usecase.LoginWithPhoneUseCase
import com.dadadrive.domain.usecase.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val loginWithPhoneUseCase: LoginWithPhoneUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = loginUseCase(email, password)
            _authState.value = result.fold(
                onSuccess = { user -> AuthState.Success(user) },
                onFailure = { e -> AuthState.Error(e.message ?: "Login failed") }
            )
        }
    }

    fun signup(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        profilePictureUri: String? = null
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = signupUseCase(fullName, email, password, phoneNumber, profilePictureUri)
            _authState.value = result.fold(
                onSuccess = { user -> AuthState.Success(user) },
                onFailure = { e -> AuthState.Error(e.message ?: "Signup failed") }
            )
        }
    }

    fun loginWithPhone(phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = loginWithPhoneUseCase(phoneNumber)
            _authState.value = result.fold(
                onSuccess = { user -> AuthState.Success(user) },
                onFailure = { e -> AuthState.Error(e.message ?: "Erreur de connexion") }
            )
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
