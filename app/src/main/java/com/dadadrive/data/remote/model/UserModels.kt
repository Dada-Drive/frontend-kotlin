package com.dadadrive.data.remote.model

import com.google.gson.annotations.SerializedName

data class UpdateRoleRequest(val role: String)

data class UpdateRoleResponse(
    val success: Boolean,
    val user: UserDto
)

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String? = null,
    val email: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class UpdateProfileResponse(
    val success: Boolean,
    val user: UserDto
)
