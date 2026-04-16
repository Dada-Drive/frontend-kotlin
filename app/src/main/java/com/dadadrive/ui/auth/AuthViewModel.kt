package com.dadadrive.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.domain.usecase.GoogleAuthUseCase
import com.dadadrive.domain.usecase.LoginUseCase
import com.dadadrive.domain.usecase.LoginWithPhoneUseCase
import com.dadadrive.domain.usecase.SendOtpUseCase
import com.dadadrive.domain.usecase.SignupUseCase
import com.dadadrive.domain.usecase.VerifyOtpUseCase
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
    private val loginWithPhoneUseCase: LoginWithPhoneUseCase,
    private val googleAuthUseCase: GoogleAuthUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = loginUseCase(phone, password)
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

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = googleAuthUseCase(idToken)
            _authState.value = result.fold(
                onSuccess = { user ->
                    // Si pas de numéro de téléphone → demander la vérification
                    if (user.phoneNumber.isBlank()) AuthState.NeedsPhone(user)
                    else AuthState.Success(user)
                },
                onFailure = { e -> AuthState.Error(e.message ?: "Erreur de connexion Google") }
            )
        }
    }

    fun sendOtp(phone: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = sendOtpUseCase(phone)
            _authState.value = result.fold(
                onSuccess = { AuthState.OtpSent },
                onFailure = { e -> AuthState.Error(e.message ?: "Erreur d'envoi du code") }
            )
        }
    }

    fun verifyOtp(phone: String, code: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = verifyOtpUseCase(phone, code)
            _authState.value = result.fold(
                onSuccess = { user -> AuthState.Success(user) },
                onFailure = { e -> AuthState.Error(e.message ?: "Code incorrect") }
            )
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
