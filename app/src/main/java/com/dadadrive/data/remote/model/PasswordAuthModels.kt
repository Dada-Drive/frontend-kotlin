package com.dadadrive.data.remote.model

import com.google.gson.annotations.SerializedName

/** POST /auth/login — body aligné sur `AuthService.login`. */
data class LoginRequest(
    val phone: String,
    val password: String
)

/** POST /auth/register — body aligné sur `AuthService.register`. */
data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    val email: String?,
    val phone: String?,
    val password: String
)
