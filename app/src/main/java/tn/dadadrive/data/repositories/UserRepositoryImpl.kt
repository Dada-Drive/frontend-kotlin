package tn.dadadrive.data.repositories

import android.util.Log
import tn.dadadrive.data.local.SessionProfileCache
import tn.dadadrive.data.network.ApiErrorParser
import tn.dadadrive.data.network.api.AuthApiService
import tn.dadadrive.data.network.model.UpdateProfileRequest
import tn.dadadrive.data.network.model.UpdateRoleRequest
import tn.dadadrive.data.network.model.toDomainUser
import tn.dadadrive.data.storage.UserManager
import tn.dadadrive.domain.models.User
import tn.dadadrive.domain.protocols.UserRepository
import retrofit2.HttpException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val userManager: UserManager,
    private val sessionProfileCache: SessionProfileCache,
) : UserRepository {

    override suspend fun getMe(): Result<User> {
        return try {
            val response = authApiService.getMe()
            val user = response.user.toDomainUser()
            userManager.saveUser(user)
            sessionProfileCache.save(user)
            Result.success(user)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(
        fullName: String?,
        email: String?,
        avatarUrl: String?,
    ): Result<User> {
        return try {
            val response = authApiService.updateProfile(
                UpdateProfileRequest(fullName = fullName, email = email, avatarUrl = avatarUrl),
            )
            val user = response.user.toDomainUser(
                profilePictureOverride = avatarUrl ?: response.user.avatarUrl,
            )
            userManager.saveUser(user)
            sessionProfileCache.save(user)
            Result.success(user)
        } catch (e: HttpException) {
            Result.failure(Exception(ApiErrorParser.httpMessage(e)))
        } catch (e: Exception) {
            Log.w("UserRepository", "updateProfile failed", e)
            Result.failure(e)
        }
    }

    override suspend fun updateRole(role: String): Result<User> {
        return try {
            val response = authApiService.updateRole(UpdateRoleRequest(role))
            val user = response.user.toDomainUser()
            userManager.saveUser(user)
            sessionProfileCache.save(user)
            Result.success(user)
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string().orEmpty()
            if (e.code() == 400 && body.contains("already been set", ignoreCase = true)) {
                userManager.updateRole(role)
                val local = userManager.getUser() ?: return Result.failure(
                    Exception("Rôle déjà défini — impossible de charger le profil."),
                )
                sessionProfileCache.save(local)
                return Result.success(local)
            }
            val message = when (e.code()) {
                400 -> "Rôle invalide."
                401 -> "Session expirée. Reconnectez-vous."
                else -> "Erreur serveur (${e.code()})."
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            userManager.updateRole(role)
            val local = userManager.getUser() ?: return Result.failure(e)
            sessionProfileCache.save(local)
            Result.success(local)
        }
    }
}
