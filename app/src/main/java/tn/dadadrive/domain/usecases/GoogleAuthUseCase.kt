package tn.dadadrive.domain.usecases

import tn.dadadrive.domain.models.User
import tn.dadadrive.domain.protocols.AuthRepository
import javax.inject.Inject

class GoogleAuthUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(idToken: String): Result<User> {
        if (idToken.isBlank())
            return Result.failure(IllegalArgumentException("Token Google invalide"))
        return repository.loginWithGoogle(idToken)
    }
}
