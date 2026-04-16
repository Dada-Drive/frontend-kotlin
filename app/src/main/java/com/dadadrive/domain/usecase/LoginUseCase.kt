package com.dadadrive.domain.usecase

import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Connexion mot de passe — alignée sur `POST /auth/login` (téléphone + mot de passe).
 */
class LoginUseCase @Inject constructor(private val repository: AuthRepository) {

    companion object {
        private val PHONE_REGEX = Regex("^[+]?[0-9]{8,15}$")
    }

    suspend operator fun invoke(phone: String, password: String): Result<User> {
        val clean = phone.replace(" ", "").replace("-", "").trim()
        if (clean.isBlank())
            return Result.failure(IllegalArgumentException("Numéro de téléphone requis"))
        if (!PHONE_REGEX.matches(clean))
            return Result.failure(IllegalArgumentException("Numéro de téléphone invalide (8–15 chiffres)"))
        if (password.isBlank())
            return Result.failure(IllegalArgumentException("Mot de passe requis"))
        if (password.length < 8)
            return Result.failure(IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères"))

        return repository.login(clean, password)
    }
}
