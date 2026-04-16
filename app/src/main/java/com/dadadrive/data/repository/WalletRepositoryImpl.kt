package com.dadadrive.data.repository

import com.dadadrive.data.remote.api.WalletApiService
import com.dadadrive.data.remote.model.toDomain
import com.dadadrive.domain.repository.WalletRepository
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val api: WalletApiService
) : WalletRepository {

    override suspend fun getWallet() = runCatching {
        api.getWallet().wallet.toDomain()
    }

    override suspend fun getTransactions() = runCatching {
        api.getTransactions().transactions.map { it.toDomain() }
    }
}
