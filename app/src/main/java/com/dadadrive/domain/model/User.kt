package com.dadadrive.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val profilePictureUrl: String? = null,
    val storageUsedBytes: Long = 0L,
    val storageLimitBytes: Long = 15_000_000_000L
)
