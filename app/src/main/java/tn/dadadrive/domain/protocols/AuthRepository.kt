package tn.dadadrive.domain.protocols

import tn.dadadrive.data.network.model.SendOtpResponse
import tn.dadadrive.domain.models.User

interface AuthRepository {
    /** Connexion mot de passe — backend : `POST /auth/login` avec `phone` + `password`. */
    suspend fun login(phone: String, password: String): Result<User>
    suspend fun signup(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        profilePictureUri: String?
    ): Result<User>
    suspend fun loginWithPhone(phoneNumber: String): Result<User>
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun sendOtp(phone: String): Result<SendOtpResponse>
    suspend fun verifyOtp(phone: String, code: String): Result<User>
    suspend fun getCurrentUser(): Result<User>
    suspend fun logout(): Result<Unit>
}
