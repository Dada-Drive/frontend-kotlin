package com.dadadrive.data.repository

import com.dadadrive.data.local.TokenManager
import com.dadadrive.data.remote.api.AuthApiService
import com.dadadrive.data.remote.model.GoogleAuthRequest
import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

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

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val response = authApiService.googleAuth(GoogleAuthRequest(idToken))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            val user = User(
                id = response.user.id,
                fullName = response.user.fullName,
                email = response.user.email ?: "",
                phoneNumber = response.user.phoneNumber ?: "",
                profilePictureUri = response.user.avatarUrl
            )
            Result.success(user)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                400 -> "Un compte avec cet email existe déjà. Connectez-vous avec votre mot de passe."
                401 -> "Token Google invalide ou expiré."
                else -> "Erreur serveur (${e.code()}). Veuillez réessayer."
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(Exception("Impossible de se connecter au serveur. Vérifiez votre connexion."))
        }
    }
}
