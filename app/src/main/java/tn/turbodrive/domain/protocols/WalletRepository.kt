package tn.turbodrive.domain.protocols

import tn.turbodrive.domain.models.WalletInfo
import tn.turbodrive.domain.models.WalletTransaction

interface WalletRepository {
    suspend fun getWallet(): Result<WalletInfo>

    suspend fun getTransactions(): Result<List<WalletTransaction>>
}
