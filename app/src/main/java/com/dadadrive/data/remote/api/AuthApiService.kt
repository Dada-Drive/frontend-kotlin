// Équivalent Swift : AuthRepository.swift (+ chemins relatifs comme APIClient.swift : baseURL se termine par /api)
package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.model.AuthResponse
import com.dadadrive.data.remote.model.GetMeResponseDto
import com.dadadrive.data.remote.model.GoogleAuthRequest
import com.dadadrive.data.remote.model.SendOtpRequest
import com.dadadrive.data.remote.model.SendOtpResponse
import com.dadadrive.data.remote.model.UserDto
import com.dadadrive.data.remote.model.VerifyOtpRequest
import com.dadadrive.data.remote.model.VerifyOtpResponse
import com.dadadrive.data.remote.model.UpdateProfileRequest
import com.dadadrive.data.remote.model.UpdateProfileResponse
import com.dadadrive.data.remote.model.LogoutRequest
import com.dadadrive.data.remote.model.LogoutResponse
import com.dadadrive.data.remote.model.UpdateRoleRequest
import com.dadadrive.data.remote.model.UpdateRoleResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

/**
 * Chemins relatifs au même suffixe que Swift [AppConstants.API.baseURL], ex. `https://api.dadadrive.tn/api/`
 * (slash final obligatoire dans [com.dadadrive.core.constants.Constants.BASE_URL] / local.properties).
 */
interface AuthApiService {

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
