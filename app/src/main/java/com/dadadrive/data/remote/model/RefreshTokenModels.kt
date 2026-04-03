// Équivalent Swift : RefreshResponseDTO + body refresh dans APIClient.swift
package com.dadadrive.data.remote.model

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class RefreshTokenResponse(
    val success: Boolean? = null,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)

data class LogoutRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class LogoutResponse(
    val success: Boolean? = null,
    val message: String? = null
)
