package com.dadadrive.data.remote.dto

import com.dadadrive.domain.model.User
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("profile_picture_url") val profilePictureUrl: String? = null,
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("total_rides") val totalRides: Int = 0,
    @SerializedName("is_verified") val isVerified: Boolean = false
) {
    fun toDomain() = User(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
        profilePictureUrl = profilePictureUrl,
        rating = rating,
        totalRides = totalRides,
        isVerified = isVerified
    )
}

fun User.toDto() = UserDto(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    profilePictureUrl = profilePictureUrl,
    rating = rating,
    totalRides = totalRides,
    isVerified = isVerified
)
