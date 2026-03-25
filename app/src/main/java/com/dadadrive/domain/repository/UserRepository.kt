package com.dadadrive.domain.repository

import com.dadadrive.domain.model.User
import com.dadadrive.utils.Resource

interface UserRepository {
    suspend fun getUserProfile(userId: String): Resource<User>
    suspend fun updateUserProfile(user: User): Resource<User>
    suspend fun uploadProfilePicture(userId: String, imageBytes: ByteArray): Resource<String>
    suspend fun deleteAccount(userId: String): Resource<Unit>
}
