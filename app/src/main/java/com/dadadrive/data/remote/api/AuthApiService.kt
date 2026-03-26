package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.model.AuthResponse
import com.dadadrive.data.remote.model.GoogleAuthRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): AuthResponse
}
