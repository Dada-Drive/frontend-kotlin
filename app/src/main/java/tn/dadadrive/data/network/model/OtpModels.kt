package tn.dadadrive.data.network.model

import com.google.gson.annotations.SerializedName

data class SendOtpRequest(
    @SerializedName("phone") val phone: String
)

data class SendOtpResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("expiresIn") val expiresIn: Int? = null,
)

data class VerifyOtpRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("code") val code: String
)

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
    val role: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)
