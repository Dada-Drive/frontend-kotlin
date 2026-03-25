package com.dadadrive.domain.repository

import com.dadadrive.domain.model.AuthToken
import com.dadadrive.domain.model.User
import com.dadadrive.utils.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<AuthToken>
    suspend fun register(fullName: String, email: String, phone: String, password: String): Resource<AuthToken>
    suspend fun logout(): Resource<Unit>
    suspend fun refreshToken(refreshToken: String): Resource<AuthToken>
    suspend fun getCurrentUser(): Resource<User>
    fun isLoggedIn(): Boolean
}
