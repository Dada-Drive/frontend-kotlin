package com.dadadrive.domain.model

data class WalletInfo(
    val id: String,
    val ownerId: String,
    val balance: Double,
    val status: String
)

data class WalletTransaction(
    val id: String,
    val type: String,
    val amount: Double,
    val status: String,
    val note: String?,
    val createdAt: String?
)
