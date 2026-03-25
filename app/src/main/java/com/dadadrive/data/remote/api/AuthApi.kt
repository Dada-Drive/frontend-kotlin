package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.dto.AuthResponseDto
import com.dadadrive.data.remote.dto.LoginRequestDto
import com.dadadrive.data.remote.dto.RegisterRequestDto
import com.dadadrive.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): Response<AuthResponseDto>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserDto>
}
