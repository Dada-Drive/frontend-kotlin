package com.dadadrive.domain.usecase

import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository
import javax.inject.Inject

class SignupUseCase @Inject constructor(private val repository: AuthRepository) {

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val PHONE_REGEX = Regex("^[+]?[0-9]{8,15}$")
    }

    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        profilePictureUri: String? = null
    ): Result<User> {
        if (fullName.isBlank())
            return Result.failure(IllegalArgumentException("Full name cannot be empty"))
        if (email.isBlank())
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        if (!EMAIL_REGEX.matches(email.trim()))
            return Result.failure(IllegalArgumentException("Invalid email format"))
        if (password.isBlank())
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        if (password.length < 8)
            return Result.failure(IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères"))
        if (phoneNumber.isBlank())
            return Result.failure(IllegalArgumentException("Phone number cannot be empty"))
        if (!PHONE_REGEX.matches(phoneNumber.trim()))
            return Result.failure(IllegalArgumentException("Invalid phone number (8-15 digits)"))

        return repository.signup(fullName.trim(), email.trim(), password, phoneNumber.trim(), profilePictureUri)
    }
}
