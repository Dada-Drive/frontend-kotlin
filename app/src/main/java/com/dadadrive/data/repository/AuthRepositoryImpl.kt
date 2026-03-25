package com.dadadrive.data.repository

import android.content.SharedPreferences
import com.dadadrive.data.local.dao.UserDao
import com.dadadrive.data.local.entity.toEntity
import com.dadadrive.data.remote.api.AuthApi
import com.dadadrive.data.remote.dto.LoginRequestDto
import com.dadadrive.data.remote.dto.RegisterRequestDto
import com.dadadrive.domain.model.AuthToken
import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository
import com.dadadrive.utils.Resource
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences
) : AuthRepository {

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    override suspend fun login(email: String, password: String): Resource<AuthToken> {
        return try {
            val response = authApi.login(LoginRequestDto(email, password))
            if (response.isSuccessful) {
                val token = response.body()!!.toDomain()
                sharedPreferences.edit()
                    .putString(KEY_ACCESS_TOKEN, token.accessToken)
                    .putString(KEY_REFRESH_TOKEN, token.refreshToken)
                    .apply()
                Resource.Success(token)
            } else {
                Resource.Error("Identifiants incorrects (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Resource<AuthToken> {
        return try {
            val response = authApi.register(RegisterRequestDto(fullName, email, phone, password))
            if (response.isSuccessful) {
                val token = response.body()!!.toDomain()
                sharedPreferences.edit()
                    .putString(KEY_ACCESS_TOKEN, token.accessToken)
                    .putString(KEY_REFRESH_TOKEN, token.refreshToken)
                    .apply()
                Resource.Success(token)
            } else {
                Resource.Error("Échec de l'inscription (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            authApi.logout()
            sharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply()
            userDao.clearAll()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Erreur lors de la déconnexion : ${e.localizedMessage}", e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): Resource<AuthToken> {
        return try {
            val response = authApi.refreshToken(mapOf("refresh_token" to refreshToken))
            if (response.isSuccessful) {
                val token = response.body()!!.toDomain()
                sharedPreferences.edit()
                    .putString(KEY_ACCESS_TOKEN, token.accessToken)
                    .putString(KEY_REFRESH_TOKEN, token.refreshToken)
                    .apply()
                Resource.Success(token)
            } else {
                Resource.Error("Session expirée, veuillez vous reconnecter")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val response = authApi.getCurrentUser()
            if (response.isSuccessful) {
                val user = response.body()!!.toDomain()
                userDao.insertUser(user.toEntity())
                Resource.Success(user)
            } else {
                val cached = userDao.getUserById(sharedPreferences.getString("user_id", "") ?: "")
                if (cached != null) Resource.Success(cached.toDomain())
                else Resource.Error("Impossible de récupérer le profil (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override fun isLoggedIn(): Boolean {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null) != null
    }
}
