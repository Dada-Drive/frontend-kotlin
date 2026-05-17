// Équivalent Swift : AuthRepository.swift (+ chemins relatifs comme APIClient.swift : baseURL se termine par /api)
package tn.turbodrive.data.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import tn.turbodrive.data.network.envelope.ApiResponse
import tn.turbodrive.data.network.model.AuthResponse
import tn.turbodrive.data.network.model.GetMeResponseDto
import tn.turbodrive.data.network.model.GoogleAuthRequest
import tn.turbodrive.data.network.model.LoginRequest
import tn.turbodrive.data.network.model.LogoutRequest
import tn.turbodrive.data.network.model.RegisterRequest
import tn.turbodrive.data.network.model.SendOtpRequest
import tn.turbodrive.data.network.model.SendOtpResponse
import tn.turbodrive.data.network.model.UpdateProfileRequest
import tn.turbodrive.data.network.model.UpdateProfileResponse
import tn.turbodrive.data.network.model.UpdateRoleRequest
import tn.turbodrive.data.network.model.UpdateRoleResponse
import tn.turbodrive.data.network.model.VerifyOtpRequest
import tn.turbodrive.data.network.model.VerifyOtpResponse

/**
 * Chemins relatifs au même suffixe que Swift [AppConstants.API.baseURL], ex. `https://api.turbodrive.tn/api/`
 * (slash final obligatoire dans [tn.turbodrive.core.constants.Constants.BASE_URL] / local.properties).
 *
 * **Convention R-1.1** : toutes les méthodes retournent `Response<ApiResponse<T>>` — voir
 * [tn.turbodrive.data.network.envelope.ApiResponse] + extension `unwrap()` côté repository.
 */
interface AuthApiService {
    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest,
    ): Response<ApiResponse<AuthResponse>>

    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequest,
    ): Response<ApiResponse<AuthResponse>>

    @POST("auth/google")
    suspend fun googleAuth(
        @Body request: GoogleAuthRequest,
    ): Response<ApiResponse<AuthResponse>>

    @POST("auth/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequest,
    ): Response<ApiResponse<SendOtpResponse>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest,
    ): Response<ApiResponse<VerifyOtpResponse>>

    // AuthInterceptor ajoute automatiquement "Authorization: Bearer <token>"
    @GET("auth/me")
    suspend fun getMe(): Response<ApiResponse<GetMeResponseDto>>

    @PATCH("users/me/role")
    suspend fun updateRole(
        @Body body: UpdateRoleRequest,
    ): Response<ApiResponse<UpdateRoleResponse>>

    @PATCH("users/me")
    suspend fun updateProfile(
        @Body body: UpdateProfileRequest,
    ): Response<ApiResponse<UpdateProfileResponse>>

    @POST("auth/logout")
    suspend fun logout(
        @Body body: LogoutRequest,
    ): Response<ApiResponse<Unit>>
}
