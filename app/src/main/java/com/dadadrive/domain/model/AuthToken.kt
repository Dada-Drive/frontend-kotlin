package com.dadadrive.domain.model

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)
