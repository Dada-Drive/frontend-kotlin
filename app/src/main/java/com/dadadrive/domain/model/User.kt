package com.dadadrive.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val profilePictureUrl: String? = null,
    val rating: Double = 0.0,
    val totalRides: Int = 0,
    val isVerified: Boolean = false
)
