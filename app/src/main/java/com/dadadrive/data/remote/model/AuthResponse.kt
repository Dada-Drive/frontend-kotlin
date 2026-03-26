package com.dadadrive.data.remote.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val success: Boolean,
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    @SerializedName("full_name") val fullName: String,
    val email: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    val role: String
)
