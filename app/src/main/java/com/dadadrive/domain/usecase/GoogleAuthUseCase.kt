package com.dadadrive.domain.usecase

import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleAuthUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(idToken: String): Result<User> {
        if (idToken.isBlank())
            return Result.failure(IllegalArgumentException("Token Google invalide"))
        return repository.loginWithGoogle(idToken)
    }
}
