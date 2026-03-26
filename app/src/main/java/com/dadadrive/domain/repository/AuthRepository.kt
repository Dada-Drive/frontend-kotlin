package com.dadadrive.domain.repository

import com.dadadrive.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signup(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        profilePictureUri: String?
    ): Result<User>
    suspend fun loginWithPhone(phoneNumber: String): Result<User>
    suspend fun loginWithGoogle(idToken: String): Result<User>
}
