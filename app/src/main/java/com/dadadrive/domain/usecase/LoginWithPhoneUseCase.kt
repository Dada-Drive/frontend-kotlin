package com.dadadrive.domain.usecase

import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithPhoneUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(phoneNumber: String): Result<User> {
        val clean = phoneNumber.replace(" ", "").replace("-", "").trim()
        if (clean.isBlank()) return Result.failure(Exception("Numéro de téléphone requis"))
        return repository.loginWithPhone(clean)
    }
}
