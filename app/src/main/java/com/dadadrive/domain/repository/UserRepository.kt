package com.dadadrive.domain.repository

import com.dadadrive.domain.model.User
import com.dadadrive.utils.Result

interface UserRepository {
    suspend fun getCurrentUser(): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, displayName: String): Result<User>
    suspend fun logout(): Result<Unit>
}
