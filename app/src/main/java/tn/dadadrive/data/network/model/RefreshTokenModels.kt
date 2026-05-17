// Équivalent Swift : RefreshResponseDTO + body refresh dans APIClient.swift
package tn.dadadrive.data.network.model

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String,
)

data class RefreshTokenResponse(
    val success: Boolean? = null,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
)

data class LogoutRequest(
    @SerializedName("refreshToken") val refreshToken: String,
)

// LogoutResponse supprimé en R-1.1 : `POST /auth/logout` est désormais typé `ApiResponse<Unit>`
// (cf. AuthApiService.logout) — le payload `{message?}` est ignoré côté repo.
