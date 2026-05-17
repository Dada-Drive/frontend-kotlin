package tn.turbodrive.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.turbodrive.data.network.RateLimitException
import tn.turbodrive.domain.usecases.GoogleAuthUseCase
import tn.turbodrive.domain.usecases.LoginUseCase
import tn.turbodrive.domain.usecases.LoginWithPhoneUseCase
import tn.turbodrive.domain.usecases.SendOtpUseCase
import tn.turbodrive.domain.usecases.SignupUseCase
import tn.turbodrive.domain.usecases.VerifyOtpUseCase
import javax.inject.Inject

sealed class OtpUiState {
    object Idle : OtpUiState()

    object SendingOtp : OtpUiState()

    data class OtpSent(val phone: String) : OtpUiState()

    data class VerifyingOtp(val phone: String) : OtpUiState()

    data class Success(val token: String) : OtpUiState()

    data class Error(val message: String) : OtpUiState()
}

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val loginUseCase: LoginUseCase,
        private val signupUseCase: SignupUseCase,
        private val loginWithPhoneUseCase: LoginWithPhoneUseCase,
        private val googleAuthUseCase: GoogleAuthUseCase,
        private val sendOtpUseCase: SendOtpUseCase,
        private val verifyOtpUseCase: VerifyOtpUseCase,
    ) : ViewModel() {
        private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
        val authState: StateFlow<AuthState> = _authState.asStateFlow()

        private val _otpState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
        val otpState: StateFlow<OtpUiState> = _otpState.asStateFlow()

        private val _resendCooldown = MutableStateFlow(0)
        val resendCooldown: StateFlow<Int> = _resendCooldown.asStateFlow()

        // Cooldown déclenché quand le backend renvoie 429 sur /auth/google. Tant qu'il est > 0,
        // on bloque tout nouvel appel (le chooser Google n'est même pas ouvert) — sinon chaque
        // nouveau tap rebrosserait dans la même fenêtre de rate limit et renverrait 429.
        private val _googleCooldown = MutableStateFlow(0)
        val googleCooldown: StateFlow<Int> = _googleCooldown.asStateFlow()
        private var googleCooldownJob: Job? = null

        fun login(
            phone: String,
            password: String,
        ) {
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                val result = loginUseCase(phone, password)
                _authState.value =
                    result.fold(
                        onSuccess = { user -> AuthState.Success(user) },
                        onFailure = { e -> AuthState.Error(e.message ?: "Login failed") },
                    )
            }
        }

        fun signup(
            fullName: String,
            email: String,
            password: String,
            phoneNumber: String,
            profilePictureUri: String? = null,
        ) {
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                val result = signupUseCase(fullName, email, password, phoneNumber, profilePictureUri)
                _authState.value =
                    result.fold(
                        onSuccess = { user -> AuthState.Success(user) },
                        onFailure = { e -> AuthState.Error(e.message ?: "Signup failed") },
                    )
            }
        }

        fun loginWithPhone(phoneNumber: String) {
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                val result = loginWithPhoneUseCase(phoneNumber)
                _authState.value =
                    result.fold(
                        onSuccess = { user -> AuthState.Success(user) },
                        onFailure = { e -> AuthState.Error(e.message ?: "Erreur de connexion") },
                    )
            }
        }

        fun loginWithGoogle(idToken: String) {
            // Court-circuite si on est déjà rate-limité : on évite un aller-retour réseau
            // qui rechargerait à coup sûr un 429 et prolongerait la fenêtre côté backend.
            if (_googleCooldown.value > 0) return
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                val result = googleAuthUseCase(idToken)
                _authState.value =
                    result.fold(
                        onSuccess = { user ->
                            if (user.phoneNumber.isBlank()) {
                                AuthState.NeedsPhone(user)
                            } else {
                                AuthState.Success(user)
                            }
                        },
                        onFailure = { e ->
                            if (e is RateLimitException) startGoogleCooldown(e.retryAfterSeconds)
                            AuthState.Error(e.message ?: "Erreur de connexion Google")
                        },
                    )
            }
        }

        private fun startGoogleCooldown(seconds: Int) {
            googleCooldownJob?.cancel()
            googleCooldownJob =
                viewModelScope.launch {
                    _googleCooldown.value = seconds.coerceAtLeast(1)
                    while (_googleCooldown.value > 0) {
                        delay(1000)
                        _googleCooldown.value -= 1
                    }
                }
        }

        fun sendOtp(phone: String) {
            viewModelScope.launch {
                _otpState.value = OtpUiState.SendingOtp
                sendOtpUseCase(phone)
                    .onSuccess {
                        _otpState.value = OtpUiState.OtpSent(phone)
                        startResendCooldown()
                    }
                    .onFailure { e ->
                        _otpState.value =
                            OtpUiState.Error(
                                e.message ?: "Could not send OTP",
                            )
                    }
            }
        }

        fun verifyOtp(
            phone: String,
            code: String,
        ) {
            viewModelScope.launch {
                _otpState.value = OtpUiState.VerifyingOtp(phone)
                verifyOtpUseCase(phone, code)
                    .onSuccess {
                        _otpState.value = OtpUiState.Success("")
                    }
                    .onFailure { e ->
                        _otpState.value =
                            OtpUiState.Error(
                                e.message ?: "Invalid OTP code",
                            )
                    }
            }
        }

        private fun startResendCooldown() {
            viewModelScope.launch {
                _resendCooldown.value = 60
                while (_resendCooldown.value > 0) {
                    delay(1000)
                    _resendCooldown.value -= 1
                }
            }
        }

        fun resetOtpState() {
            _otpState.value = OtpUiState.Idle
        }

        fun resetState() {
            _authState.value = AuthState.Idle
        }

        /** Google Sign-In / Credential errors before a usable id_token is sent to the backend. */
        fun reportGoogleCredentialError(message: String) {
            _authState.value = AuthState.Error(message)
        }
    }
