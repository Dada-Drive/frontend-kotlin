package tn.dadadrive.domain.protocols

import tn.dadadrive.domain.models.WalletInfo
import tn.dadadrive.domain.models.WalletTransaction

interface WalletRepository {
    suspend fun getWallet(): Result<WalletInfo>
    suspend fun getTransactions(): Result<List<WalletTransaction>>
}
