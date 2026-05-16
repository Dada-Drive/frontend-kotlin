package tn.dadadrive.data.repositories

import tn.dadadrive.data.network.api.WalletApiService
import tn.dadadrive.data.network.model.toDomain
import tn.dadadrive.domain.protocols.WalletRepository
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
