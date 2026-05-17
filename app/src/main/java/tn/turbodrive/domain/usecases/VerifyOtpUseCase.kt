package tn.turbodrive.domain.usecases

import tn.turbodrive.core.constants.Constants
import tn.turbodrive.domain.models.User
import tn.turbodrive.domain.protocols.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase
    @Inject
    constructor(private val repository: AuthRepository) {
        suspend operator fun invoke(
            phone: String,
            code: String,
        ): Result<User> {
            if (phone.isBlank()) {
                return Result.failure(IllegalArgumentException("Numéro de téléphone requis"))
            }
            if (code.length != Constants.PHONE_CODE_LENGTH) {
                return Result.failure(IllegalArgumentException("Code invalide"))
            }
            return repository.verifyOtp(phone, code)
        }
    }
