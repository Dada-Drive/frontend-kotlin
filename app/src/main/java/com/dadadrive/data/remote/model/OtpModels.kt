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
    val accessToken: String,
    val refreshToken: String,
    val user: OtpUserDto,
    val isNewUser: Boolean
)

data class OtpUserDto(
    val id: String,
    @SerializedName("full_name") val fullName: String?,
    val email: String?,
    val phone: String?,
    val role: String
)
