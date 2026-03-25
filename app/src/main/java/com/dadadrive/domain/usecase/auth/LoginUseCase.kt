package com.dadadrive.domain.usecase.auth

import com.dadadrive.domain.model.AuthToken
import com.dadadrive.domain.repository.AuthRepository
import com.dadadrive.utils.Resource
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Resource<AuthToken> {
        if (email.isBlank() || !email.contains("@")) {
            return Resource.Error("Adresse email invalide")
        }
        if (password.length < 6) {
            return Resource.Error("Le mot de passe doit contenir au moins 6 caractères")
        }
        return authRepository.login(email.trim(), password)
    }
}
