package com.dadadrive.data.remote.model

import com.google.gson.annotations.SerializedName

data class SendOtpRequest(val phone: String)

data class SendOtpResponse(
    val success: Boolean,
    val message: String
)

data class VerifyOtpRequest(val phone: String, val code: String)

data class VerifyOtpResponse(
    val success: Boolean,
    val message: String? = null,
    // null quand l'utilisateur est déjà connecté (Google) → backend ne renvoie pas de nouveaux tokens
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: OtpUserDto? = null,
    val isNewUser: Boolean = false
)

data class OtpUserDto(
    val id: String,
    @SerializedName("full_name") val fullName: String?,
    val email: String?,
    val phone: String?,
    val role: String
)
