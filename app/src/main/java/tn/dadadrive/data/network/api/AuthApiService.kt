// Équivalent Swift : AuthRepository.swift (+ chemins relatifs comme APIClient.swift : baseURL se termine par /api)
package tn.dadadrive.data.network.api

import tn.dadadrive.data.network.model.AuthResponse
import tn.dadadrive.data.network.model.GetMeResponseDto
import tn.dadadrive.data.network.model.GoogleAuthRequest
import tn.dadadrive.data.network.model.SendOtpRequest
import tn.dadadrive.data.network.model.SendOtpResponse
import tn.dadadrive.data.network.model.UserDto
import tn.dadadrive.data.network.model.VerifyOtpRequest
import tn.dadadrive.data.network.model.VerifyOtpResponse
import tn.dadadrive.data.network.model.UpdateProfileRequest
import tn.dadadrive.data.network.model.UpdateProfileResponse
import tn.dadadrive.data.network.model.LoginRequest
import tn.dadadrive.data.network.model.LogoutRequest
import tn.dadadrive.data.network.model.LogoutResponse
import tn.dadadrive.data.network.model.RegisterRequest
import tn.dadadrive.data.network.model.UpdateRoleRequest
import tn.dadadrive.data.network.model.UpdateRoleResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

/**
 * Chemins relatifs au même suffixe que Swift [AppConstants.API.baseURL], ex. `https://api.dadadrive.tn/api/`
 * (slash final obligatoire dans [tn.dadadrive.core.constants.Constants.BASE_URL] / local.properties).
 */
interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): AuthResponse

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse

    // AuthInterceptor ajoute automatiquement "Authorization: Bearer <token>"
    @GET("auth/me")
    suspend fun getMe(): GetMeResponseDto

    @PATCH("users/me/role")
    suspend fun updateRole(@Body body: UpdateRoleRequest): UpdateRoleResponse

    @PATCH("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UpdateProfileResponse

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): LogoutResponse
}
