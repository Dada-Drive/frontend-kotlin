package com.dadadrive.domain.usecase

import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(phone: String, code: String): Result<User> {
        if (phone.isBlank())
            return Result.failure(IllegalArgumentException("Numéro de téléphone requis"))
        if (code.isBlank() || code.length < 4)
            return Result.failure(IllegalArgumentException("Code invalide"))
        return repository.verifyOtp(phone, code)
    }
}
