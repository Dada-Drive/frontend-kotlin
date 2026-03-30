package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.model.AuthResponse
import com.dadadrive.data.remote.model.GoogleAuthRequest
import com.dadadrive.data.remote.model.SendOtpRequest
import com.dadadrive.data.remote.model.SendOtpResponse
import com.dadadrive.data.remote.model.UserDto
import com.dadadrive.data.remote.model.VerifyOtpRequest
import com.dadadrive.data.remote.model.VerifyOtpResponse
import com.dadadrive.data.remote.model.UpdateProfileRequest
import com.dadadrive.data.remote.model.UpdateProfileResponse
import com.dadadrive.data.remote.model.UpdateRoleRequest
import com.dadadrive.data.remote.model.UpdateRoleResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): AuthResponse

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse

    // AuthInterceptor ajoute automatiquement "Authorization: Bearer <token>"
    @GET("api/auth/me")
    suspend fun getMe(): UserDto

    @PATCH("api/users/me/role")
    suspend fun updateRole(@Body body: UpdateRoleRequest): UpdateRoleResponse

    @PATCH("api/users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UpdateProfileResponse
}
