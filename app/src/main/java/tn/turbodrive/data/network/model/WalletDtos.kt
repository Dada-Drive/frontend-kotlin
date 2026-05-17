package tn.turbodrive.data.network.model

import com.google.gson.annotations.SerializedName
import tn.turbodrive.domain.models.WalletInfo
import tn.turbodrive.domain.models.WalletTransaction

/** Payload de `GET /wallet` — encapsulé par `ApiResponse<WalletResponseDto>`. */
data class WalletResponseDto(
    val wallet: WalletDto,
)

/** Payload de `GET /wallet/transactions` — encapsulé par `ApiResponse<WalletTransactionsResponseDto>`. */
data class WalletTransactionsResponseDto(
    val transactions: List<WalletTransactionDto>,
)

data class WalletDto(
    val id: String,
    @SerializedName("owner_id") val ownerId: String,
    val balance: String?,
    val status: String?,
)

data class WalletTransactionDto(
    val id: String,
    val type: String,
    val amount: String?,
    val status: String?,
    val note: String?,
    @SerializedName("created_at") val createdAt: String?,
)

fun WalletDto.toDomain() =
    WalletInfo(
        id = id,
        ownerId = ownerId,
        balance = balance?.toDoubleOrNull() ?: 0.0,
        status = status ?: "unknown",
    )

fun WalletTransactionDto.toDomain() =
    WalletTransaction(
        id = id,
        type = type,
        amount = amount?.toDoubleOrNull() ?: 0.0,
        status = status ?: "unknown",
        note = note,
        createdAt = createdAt,
    )
