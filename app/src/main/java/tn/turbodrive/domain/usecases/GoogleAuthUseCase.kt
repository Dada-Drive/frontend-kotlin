package tn.turbodrive.domain.usecases

import tn.turbodrive.domain.models.User
import tn.turbodrive.domain.protocols.AuthRepository
import javax.inject.Inject

class GoogleAuthUseCase
    @Inject
    constructor(private val repository: AuthRepository) {
        suspend operator fun invoke(idToken: String): Result<User> {
            if (idToken.isBlank()) {
                return Result.failure(IllegalArgumentException("Token Google invalide"))
            }
            return repository.loginWithGoogle(idToken)
        }
    }
