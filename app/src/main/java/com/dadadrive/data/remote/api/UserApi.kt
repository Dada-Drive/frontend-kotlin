package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.dto.UserDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApi {
    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<UserDto>

    @PUT("users/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body user: UserDto
    ): Response<UserDto>

    @Multipart
    @PUT("users/{userId}/picture")
    suspend fun uploadProfilePicture(
        @Path("userId") userId: String,
        @Part image: MultipartBody.Part
    ): Response<Map<String, String>>

    @DELETE("users/{userId}")
    suspend fun deleteAccount(@Path("userId") userId: String): Response<Unit>
}
