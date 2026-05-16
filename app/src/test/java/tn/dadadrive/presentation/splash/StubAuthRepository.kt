package tn.dadadrive.presentation.splash

import tn.dadadrive.data.network.model.SendOtpResponse
import tn.dadadrive.domain.models.User
import tn.dadadrive.domain.protocols.AuthRepository

/** Auth minimal pour tests unitaires (scénario piloté par [getCurrentUserBlock]). */
internal class StubAuthRepository(
    private val getCurrentUserBlock: suspend () -> Result<User>,
) : AuthRepository {

    override suspend fun getCurrentUser(): Result<User> = getCurrentUserBlock()

    override suspend fun login(phone: String, password: String): Result<User> = notUsed()

    override suspend fun signup(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        profilePictureUri: String?,
    ): Result<User> = notUsed()

    override suspend fun loginWithPhone(phoneNumber: String): Result<User> = notUsed()

    override suspend fun loginWithGoogle(idToken: String): Result<User> = notUsed()

    override suspend fun sendOtp(phone: String): Result<SendOtpResponse> = notUsed()

    override suspend fun verifyOtp(phone: String, code: String): Result<User> = notUsed()

    override suspend fun logout(): Result<Unit> = notUsed()

    private fun <T> notUsed(): T = error("StubAuthRepository: not used in this test")
}
