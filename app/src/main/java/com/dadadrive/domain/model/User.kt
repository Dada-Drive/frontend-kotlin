package com.dadadrive.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: String = "rider",
    val profilePictureUri: String? = null
)
