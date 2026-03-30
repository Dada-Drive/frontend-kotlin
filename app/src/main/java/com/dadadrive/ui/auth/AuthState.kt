package com.dadadrive.ui.auth

import com.dadadrive.domain.model.User

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    data class Success(val user: User) : AuthState()
    data class NeedsPhone(val user: User) : AuthState() // Google user sans numéro
    data class Error(val message: String) : AuthState()
}
