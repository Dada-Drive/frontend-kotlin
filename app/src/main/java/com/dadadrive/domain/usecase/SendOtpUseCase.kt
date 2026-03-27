package com.dadadrive.domain.usecase

import com.dadadrive.domain.repository.AuthRepository
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(phone: String): Result<Unit> {
        if (phone.isBlank())
            return Result.failure(IllegalArgumentException("Numéro de téléphone requis"))
        return repository.sendOtp(phone)
    }
}
