package com.dadadrive.domain.usecase

import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank())
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        if (!EMAIL_REGEX.matches(email.trim()))
            return Result.failure(IllegalArgumentException("Invalid email format"))
        if (password.isBlank())
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        if (password.length < 6)
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))

        return repository.login(email.trim(), password)
    }
}
