package tn.dadadrive.presentation.auth

import tn.dadadrive.domain.models.User

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class NeedsPhone(val user: User) : AuthState() // Google user sans numéro
    data class Error(val message: String) : AuthState()
}
