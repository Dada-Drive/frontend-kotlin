package com.dadadrive.data.remote.model

import com.dadadrive.domain.model.WalletInfo
import com.dadadrive.domain.model.WalletTransaction
import com.google.gson.annotations.SerializedName

data class WalletResponseDto(
    val success: Boolean? = null,
    val wallet: WalletDto
)

data class WalletTransactionsResponseDto(
    val success: Boolean? = null,
    val transactions: List<WalletTransactionDto>
)

data class WalletDto(
    val id: String,
    @SerializedName("owner_id") val ownerId: String,
    val balance: String?,
    val status: String?
)

data class WalletTransactionDto(
    val id: String,
    val type: String,
    val amount: String?,
    val status: String?,
    val note: String?,
    @SerializedName("created_at") val createdAt: String?
)

fun WalletDto.toDomain() = WalletInfo(
    id = id,
    ownerId = ownerId,
    balance = balance?.toDoubleOrNull() ?: 0.0,
    status = status ?: "unknown"
)

fun WalletTransactionDto.toDomain() = WalletTransaction(
    id = id,
    type = type,
    amount = amount?.toDoubleOrNull() ?: 0.0,
    status = status ?: "unknown",
    note = note,
    createdAt = createdAt
)
