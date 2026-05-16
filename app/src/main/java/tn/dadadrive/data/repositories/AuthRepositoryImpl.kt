package tn.dadadrive.data.repositories

import android.net.Uri
import android.util.Log
import tn.dadadrive.core.phone.normalizePhoneForOtpBackend
import tn.dadadrive.data.storage.TokenManager
import tn.dadadrive.data.storage.UserManager
import tn.dadadrive.data.network.ApiErrorParser
import tn.dadadrive.data.network.RateLimitException
import tn.dadadrive.data.network.retryAfterSeconds
import tn.dadadrive.data.network.api.AuthApiService
import tn.dadadrive.data.network.cloudinary.CloudinaryManager
import tn.dadadrive.data.network.model.GoogleAuthRequest
import tn.dadadrive.data.network.model.LoginRequest
import tn.dadadrive.data.network.model.RegisterRequest
import tn.dadadrive.data.network.model.SendOtpRequest
import tn.dadadrive.data.network.model.SendOtpResponse
import tn.dadadrive.data.network.model.LogoutRequest
import tn.dadadrive.data.network.model.VerifyOtpRequest
import tn.dadadrive.data.network.model.toDomainUser
import tn.dadadrive.data.local.SessionProfileCache
import tn.dadadrive.domain.models.User
import tn.dadadrive.domain.protocols.AuthRepository
import tn.dadadrive.domain.protocols.UserRepository
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val cloudinaryManager: CloudinaryManager,
    private val sessionProfileCache: SessionProfileCache,
    private val userRepository: UserRepository,
) : AuthRepository {

    override suspend fun login(phone: String, password: String): Result<User> {
        return try {
            val response = authApiService.login(LoginRequest(phone = phone, password = password))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            val user = User(
                id = response.user.id,
                fullName = response.user.fullName ?: "",
                email = response.user.email ?: "",
                phoneNumber = response.user.phone ?: phone,
                role = response.user.role.ifBlank { "rider" },
                profilePictureUri = response.user.avatarUrl
            )
            userManager.saveUser(user)
            Result.success(user)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                401 -> ApiErrorParser.httpMessage(e).ifBlank { "Identifiants invalides." }
                429 -> "Trop de tentatives. Réessayez plus tard."
                else -> ApiErrorParser.httpMessage(e)
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(Exception("Impossible de se connecter au serveur. Vérifiez votre connexion."))
        }
    }

    override suspend fun loginWithPhone(phoneNumber: String): Result<User> {
        return Result.failure(
            Exception("Connexion par numéro seul indisponible. Utilisez le code OTP ou le mot de passe.")
        )
    }

    override suspend fun signup(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        profilePictureUri: String?
    ): Result<User> {
        return try {
            val response = authApiService.register(
                RegisterRequest(
                    fullName = fullName,
                    email = email.ifBlank { null },
                    phone = phoneNumber.ifBlank { null },
                    password = password
                )
            )
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            var user = User(
                id = response.user.id,
                fullName = response.user.fullName ?: fullName,
                email = response.user.email ?: email,
                phoneNumber = response.user.phone ?: phoneNumber,
                role = response.user.role.ifBlank { "pending" },
                profilePictureUri = response.user.avatarUrl
            )
            userManager.saveUser(user)

            if (!profilePictureUri.isNullOrBlank()) {
                val publicId = cloudinaryManager.publicIdForUser(user.id)
                val uploadResult = cloudinaryManager.uploadImage(Uri.parse(profilePictureUri), publicId)
                if (uploadResult.isSuccess) {
                    val cloudUrl = uploadResult.getOrThrow()
                    userRepository.updateProfile(avatarUrl = cloudUrl).onFailure { e ->
                        Log.w("AuthRepo", "Avatar sync après inscription échoué (non bloquant)", e)
                    }
                    user = user.copy(profilePictureUri = cloudUrl)
                    userManager.saveUser(user)
                    userManager.saveCloudinaryPublicId(publicId)
                } else {
                    uploadResult.exceptionOrNull()?.let { e ->
                        Log.w("AuthRepo", "Upload Cloudinary inscription échoué (non bloquant)", e)
                    }
                }
            }

            Result.success(user)
        } catch (e: HttpException) {
            Result.failure(Exception(ApiErrorParser.httpMessage(e)))
        } catch (e: Exception) {
            Result.failure(Exception("Impossible de se connecter au serveur. Vérifiez votre connexion."))
        }
    }

    override suspend fun sendOtp(phone: String): Result<SendOtpResponse> {
        return try {
            val normalized = normalizePhoneForOtpBackend(phone)
            if (normalized.isEmpty()) {
                return Result.failure(Exception("Numéro de téléphone invalide."))
            }
            val response = authApiService.sendOtp(SendOtpRequest(phone = normalized))
            Result.success(response)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                429 -> "Trop de tentatives. Veuillez réessayer plus tard."
                400 -> ApiErrorParser.httpMessage(e).ifBlank { "Numéro de téléphone invalide." }
                else -> ApiErrorParser.httpMessage(e).ifBlank {
                    "Erreur serveur (${e.code()}). Veuillez réessayer."
                }
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(Exception("Impossible de se connecter au serveur. Vérifiez votre connexion."))
        }
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<User> {
        return try {
            val normalizedPhone = normalizePhoneForOtpBackend(phone)
            if (normalizedPhone.isEmpty()) {
                return Result.failure(Exception("Numéro de téléphone invalide."))
            }
            val response = authApiService.verifyOtp(
                VerifyOtpRequest(phone = normalizedPhone, code = code.trim())
            )

            // Scénario 1 — utilisateur Google déjà connecté qui vérifie son numéro
            // Le backend retourne { success: true, message: "Phone verified successfully" } sans tokens
            if (response.accessToken == null) {
                val existing = userManager.getUser()
                val updatedUser = existing?.copy(phoneNumber = normalizedPhone)
                    ?: User(id = "", fullName = "", email = "", phoneNumber = normalizedPhone)
                userManager.saveUser(updatedUser)
                return Result.success(updatedUser)
            }

            // Scénario 2 — nouvel utilisateur ou login via OTP (tokens fournis)
            val refresh = response.refreshToken
            val dto = response.user
            if (refresh.isNullOrBlank() || dto == null) {
                return Result.failure(Exception("Réponse serveur invalide."))
            }
            tokenManager.saveTokens(response.accessToken, refresh)
            val user = dto.toDomainUser(phoneFallback = normalizedPhone)
            userManager.saveUser(user)
            Result.success(user)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                400 -> ApiErrorParser.httpMessage(e).ifBlank { "Ce numéro est déjà utilisé par un autre compte." }
                401 -> ApiErrorParser.httpMessage(e).ifBlank { "Code incorrect ou expiré." }
                429 -> ApiErrorParser.httpMessage(e).ifBlank { "Trop de tentatives. Demandez un nouveau code." }
                else -> ApiErrorParser.httpMessage(e).ifBlank {
                    "Erreur serveur (${e.code()}). Veuillez réessayer."
                }
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(Exception("Impossible de se connecter au serveur. Vérifiez votre connexion."))
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val response = authApiService.googleAuth(GoogleAuthRequest(idToken))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)

            val googleAvatarUrl = response.user.avatarUrl
            val userId = response.user.id
            val publicId = cloudinaryManager.publicIdForUser(userId)

            // Upload non-bloquant de la photo Google vers Cloudinary
            val finalAvatarUrl = if (!googleAvatarUrl.isNullOrBlank()) {
                val upload = cloudinaryManager.uploadFromUrl(googleAvatarUrl, publicId)
                if (upload.isSuccess) {
                    val cloudUrl = upload.getOrThrow()
                    userRepository.updateProfile(avatarUrl = cloudUrl).onFailure { e ->
                        Log.w("AuthRepo", "Sync avatar backend échoué (non bloquant)", e)
                    }
                    userManager.saveCloudinaryPublicId(publicId)
                    cloudUrl
                } else {
                    upload.exceptionOrNull()?.let { e ->
                        Log.w("AuthRepo", "Upload Cloudinary échoué — fallback Google URL", e)
                    }
                    googleAvatarUrl
                }
            } else {
                null
            }

            val user = User(
                id = userId,
                fullName = response.user.fullName ?: "",
                email = response.user.email ?: "",
                phoneNumber = response.user.phone ?: "",
                role = response.user.role.ifBlank { "rider" },
                profilePictureUri = finalAvatarUrl
            )
            userManager.saveUser(user)
            Result.success(user)
        } catch (e: HttpException) {
            if (e.code() == 429) {
                // Retry-After renvoyé par express-rate-limit (Node) — fallback à 60s si absent.
                val retryAfter = e.retryAfterSeconds(fallbackSeconds = 60)
                return Result.failure(
                    RateLimitException(
                        retryAfterSeconds = retryAfter,
                        message = "Trop de tentatives. Réessayez dans $retryAfter s."
                    )
                )
            }
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

    override suspend fun getCurrentUser(): Result<User> = userRepository.getMe()

    override suspend fun logout(): Result<Unit> {
        return try {
            authApiService.logout(LogoutRequest(tokenManager.getRefreshToken().orEmpty()))
            tokenManager.clearTokens()
            userManager.clearUser()
            sessionProfileCache.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearTokens()
            userManager.clearUser()
            sessionProfileCache.clear()
            Result.failure(e)
        }
    }
}
