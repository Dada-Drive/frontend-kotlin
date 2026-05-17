package tn.turbodrive.data.repositories

import android.util.Log
import tn.turbodrive.data.local.SessionProfileCache
import tn.turbodrive.data.network.api.AuthApiService
import tn.turbodrive.data.network.envelope.BackendException
import tn.turbodrive.data.network.envelope.unwrap
import tn.turbodrive.data.network.model.UpdateProfileRequest
import tn.turbodrive.data.network.model.UpdateRoleRequest
import tn.turbodrive.data.network.model.toDomainUser
import tn.turbodrive.data.storage.UserManager
import tn.turbodrive.domain.models.User
import tn.turbodrive.domain.protocols.UserRepository
import javax.inject.Inject

class UserRepositoryImpl
    @Inject
    constructor(
        private val authApiService: AuthApiService,
        private val userManager: UserManager,
        private val sessionProfileCache: SessionProfileCache,
    ) : UserRepository {
        override suspend fun getMe(): Result<User> =
            runCatching { authApiService.getMe().unwrap() }
                .fold(
                    onSuccess = { result ->
                        result.fold(
                            onSuccess = { response ->
                                val user = response.user.toDomainUser()
                                userManager.saveUser(user)
                                sessionProfileCache.save(user)
                                Result.success(user)
                            },
                            onFailure = { Result.failure(it) },
                        )
                    },
                    onFailure = { Result.failure(it) },
                )

        override suspend fun updateProfile(
            fullName: String?,
            email: String?,
            avatarUrl: String?,
        ): Result<User> =
            runCatching {
                authApiService
                    .updateProfile(UpdateProfileRequest(fullName = fullName, email = email, avatarUrl = avatarUrl))
                    .unwrap()
            }.fold(
                onSuccess = { result ->
                    result.fold(
                        onSuccess = { response ->
                            val user =
                                response.user.toDomainUser(
                                    profilePictureOverride = avatarUrl ?: response.user.avatarUrl,
                                )
                            userManager.saveUser(user)
                            sessionProfileCache.save(user)
                            Result.success(user)
                        },
                        onFailure = { e ->
                            val message = (e as? BackendException)?.apiError?.message ?: "Mise à jour échouée."
                            Result.failure(Exception(message))
                        },
                    )
                },
                onFailure = { e ->
                    Log.w("UserRepository", "updateProfile failed", e)
                    Result.failure(e)
                },
            )

        override suspend fun updateRole(role: String): Result<User> =
            runCatching { authApiService.updateRole(UpdateRoleRequest(role)).unwrap() }
                .fold(
                    onSuccess = { result ->
                        result.fold(
                            onSuccess = { response ->
                                val user = response.user.toDomainUser()
                                userManager.saveUser(user)
                                sessionProfileCache.save(user)
                                Result.success(user)
                            },
                            onFailure = { e ->
                                val be = e as? BackendException
                                // Backend retourne 400 "already been set" lorsque le rôle est déjà fixé.
                                // Comportement legacy : on accepte localement le rôle pour ne pas bloquer l'UX.
                                val alreadySet =
                                    be?.httpCode == 400 && be.apiError.message.contains("already been set", ignoreCase = true)
                                if (alreadySet) {
                                    userManager.updateRole(role)
                                    val local =
                                        userManager.getUser() ?: return@fold Result.failure(
                                            Exception("Rôle déjà défini — impossible de charger le profil."),
                                        )
                                    sessionProfileCache.save(local)
                                    return@fold Result.success(local)
                                }
                                val message =
                                    when (be?.httpCode) {
                                        400 -> "Rôle invalide."
                                        401 -> "Session expirée. Reconnectez-vous."
                                        else -> "Erreur serveur (${be?.httpCode ?: "?"})."
                                    }
                                Result.failure(Exception(message))
                            },
                        )
                    },
                    onFailure = { e ->
                        // IO/network failure : fallback offline (legacy behaviour)
                        userManager.updateRole(role)
                        val local = userManager.getUser() ?: return@fold Result.failure(e)
                        sessionProfileCache.save(local)
                        Result.success(local)
                    },
                )
    }
