package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.model.AuthResponse
import com.dadadrive.data.remote.model.GoogleAuthRequest
import com.dadadrive.data.remote.model.SendOtpRequest
import com.dadadrive.data.remote.model.SendOtpResponse
import com.dadadrive.data.remote.model.VerifyOtpRequest
import com.dadadrive.data.remote.model.VerifyOtpResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): AuthResponse

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse
}
