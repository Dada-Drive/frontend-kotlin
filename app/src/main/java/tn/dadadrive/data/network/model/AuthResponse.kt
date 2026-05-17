package tn.dadadrive.data.network.model

import com.google.gson.annotations.SerializedName

/**
 * Payload de login/register/googleAuth — encapsulé par `ApiResponse<AuthResponse>` côté Retrofit.
 * Le flag `success` est porté par l'enveloppe (voir [tn.dadadrive.data.network.envelope.ApiResponse]).
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto,
)

/** Payload de `GET /auth/me` — encapsulé par `ApiResponse<GetMeResponseDto>`. */
data class GetMeResponseDto(
    val user: UserDto,
)

// Équivalent Swift : User dans AuthModels.swift (CodingKeys : phone, full_name, avatar_url…)
data class UserDto(
    val id: String,
    @SerializedName("full_name") val fullName: String?,
    val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    val role: String,
)
