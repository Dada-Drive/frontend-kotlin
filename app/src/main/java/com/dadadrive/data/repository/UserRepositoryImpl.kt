package com.dadadrive.data.repository

import com.dadadrive.data.remote.api.DadaDriveApiService
import com.dadadrive.data.remote.dto.LoginRequestDto
import com.dadadrive.data.remote.dto.UserDto
import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.UserRepository
import com.dadadrive.utils.Result
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: DadaDriveApiService
) : UserRepository {

    override suspend fun getCurrentUser(): Result<User> =
        try {
            Result.Success(apiService.getCurrentUser().toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to load user profile", e)
        }

    override suspend fun login(email: String, password: String): Result<User> =
        try {
            val response = apiService.login(LoginRequestDto(email, password))
            Result.Success(response.user.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Login failed", e)
        }

    override suspend fun register(email: String, password: String, displayName: String): Result<User> =
        try {
            val response = apiService.register(LoginRequestDto(email, password, displayName))
            Result.Success(response.user.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Registration failed", e)
        }

    override suspend fun logout(): Result<Unit> = Result.Success(Unit)

    private fun UserDto.toDomain() = User(
        id = id,
        email = email,
        displayName = displayName,
        profilePictureUrl = profilePictureUrl,
        storageUsedBytes = storageUsedBytes,
        storageLimitBytes = storageLimitBytes
    )
}
