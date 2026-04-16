package com.dadadrive.domain.repository

import com.dadadrive.domain.model.WalletInfo
import com.dadadrive.domain.model.WalletTransaction

interface WalletRepository {
    suspend fun getWallet(): Result<WalletInfo>
    suspend fun getTransactions(): Result<List<WalletTransaction>>
}
