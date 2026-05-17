package tn.turbodrive.domain.protocols

import tn.turbodrive.domain.models.User

interface UserRepository {
    suspend fun getMe(): Result<User>

    suspend fun updateProfile(
        fullName: String? = null,
        email: String? = null,
        avatarUrl: String? = null,
    ): Result<User>

    suspend fun updateRole(role: String): Result<User>
}
