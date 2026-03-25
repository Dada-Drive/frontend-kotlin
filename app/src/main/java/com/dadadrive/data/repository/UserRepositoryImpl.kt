package com.dadadrive.data.repository

import com.dadadrive.data.local.dao.UserDao
import com.dadadrive.data.local.entity.toEntity
import com.dadadrive.data.remote.api.UserApi
import com.dadadrive.data.remote.dto.toDto
import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.UserRepository
import com.dadadrive.utils.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getUserProfile(userId: String): Resource<User> {
        return try {
            val response = userApi.getUserProfile(userId)
            if (response.isSuccessful) {
                val user = response.body()!!.toDomain()
                userDao.insertUser(user.toEntity())
                Resource.Success(user)
            } else {
                val cached = userDao.getUserById(userId)
                if (cached != null) Resource.Success(cached.toDomain())
                else Resource.Error("Profil introuvable (${response.code()})")
            }
        } catch (e: Exception) {
            val cached = userDao.getUserById(userId)
            if (cached != null) Resource.Success(cached.toDomain())
            else Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun updateUserProfile(user: User): Resource<User> {
        return try {
            val response = userApi.updateUserProfile(user.id, user.toDto())
            if (response.isSuccessful) {
                val updated = response.body()!!.toDomain()
                userDao.updateUser(updated.toEntity())
                Resource.Success(updated)
            } else {
                Resource.Error("Échec de la mise à jour (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun uploadProfilePicture(userId: String, imageBytes: ByteArray): Resource<String> {
        return try {
            val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", "profile.jpg", requestBody)
            val response = userApi.uploadProfilePicture(userId, part)
            if (response.isSuccessful) {
                Resource.Success(response.body()!!["url"] ?: "")
            } else {
                Resource.Error("Échec du téléchargement (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun deleteAccount(userId: String): Resource<Unit> {
        return try {
            val response = userApi.deleteAccount(userId)
            if (response.isSuccessful) {
                userDao.deleteUser(userId)
                Resource.Success(Unit)
            } else {
                Resource.Error("Impossible de supprimer le compte (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }
}
