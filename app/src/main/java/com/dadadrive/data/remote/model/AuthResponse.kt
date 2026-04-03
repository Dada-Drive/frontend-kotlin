package com.dadadrive.data.remote.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val success: Boolean,
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

/** GET /auth/me → `{ "success": true, "user": { … } }` (voir authController.getMe). */
data class GetMeResponseDto(
    val success: Boolean? = null,
    val user: UserDto
)

// Équivalent Swift : User dans AuthModels.swift (CodingKeys : phone, full_name, avatar_url…)
data class UserDto(
    val id: String,
    @SerializedName("full_name") val fullName: String?,
    val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    val role: String
)
