package com.dadadrive.data.repository

import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository
import kotlinx.coroutines.delay

class AuthRepositoryImpl : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        delay(1200)
        return Result.success(
            User(
                id = "mock-001",
                fullName = "Dada User",
                email = email,
                phoneNumber = "+1234567890"
            )
        )
    }

    override suspend fun loginWithPhone(phoneNumber: String): Result<User> {
        delay(1200)
        return Result.success(
            User(
                id = "phone-${System.currentTimeMillis()}",
                fullName = "Utilisateur DadaDrive",
                email = "",
                phoneNumber = phoneNumber
            )
        )
    }

    override suspend fun signup(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        profilePictureUri: String?
    ): Result<User> {
        delay(1500)
        return Result.success(
            User(
                id = "mock-${System.currentTimeMillis()}",
                fullName = fullName,
                email = email,
                phoneNumber = phoneNumber,
                profilePictureUri = profilePictureUri
            )
        )
    }
}
