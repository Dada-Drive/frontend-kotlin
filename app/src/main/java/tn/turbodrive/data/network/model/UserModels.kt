package tn.turbodrive.data.network.model

import com.google.gson.annotations.SerializedName

data class UpdateRoleRequest(val role: String)

data class UpdateRoleResponse(
    val user: UserDto,
)

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String? = null,
    val email: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
)

data class UpdateProfileResponse(
    val user: UserDto,
)
