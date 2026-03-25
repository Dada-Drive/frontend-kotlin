package com.dadadrive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dadadrive.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val profilePictureUrl: String? = null,
    val rating: Double = 0.0,
    val totalRides: Int = 0,
    val isVerified: Boolean = false
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

fun User.toEntity() = UserEntity(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    profilePictureUrl = profilePictureUrl,
    rating = rating,
    totalRides = totalRides,
    isVerified = isVerified
)
