package com.dadadrive.domain.usecase.auth

import com.dadadrive.domain.model.AuthToken
import com.dadadrive.domain.repository.AuthRepository
import com.dadadrive.utils.Resource
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): Resource<AuthToken> {
        if (fullName.isBlank()) return Resource.Error("Le nom complet est requis")
        if (!email.contains("@")) return Resource.Error("Adresse email invalide")
        if (phone.isBlank() || phone.length < 8) return Resource.Error("Numéro de téléphone invalide")
        if (password.length < 6) return Resource.Error("Le mot de passe doit contenir au moins 6 caractères")
        if (password != confirmPassword) return Resource.Error("Les mots de passe ne correspondent pas")
        return authRepository.register(fullName.trim(), email.trim(), phone.trim(), password)
    }
}
