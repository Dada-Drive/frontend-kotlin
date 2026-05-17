package tn.dadadrive.data.repositories

import tn.dadadrive.data.network.api.WalletApiService
import tn.dadadrive.data.network.envelope.unwrap
import tn.dadadrive.data.network.model.toDomain
import tn.dadadrive.domain.protocols.WalletRepository
import javax.inject.Inject

class WalletRepositoryImpl
    @Inject
    constructor(
        private val api: WalletApiService,
    ) : WalletRepository {
        override suspend fun getWallet() =
            runCatching { api.getWallet().unwrap() }
                .fold(
                    onSuccess = { result -> result.map { it.wallet.toDomain() } },
                    onFailure = { Result.failure(it) },
                )

        override suspend fun getTransactions() =
            runCatching { api.getTransactions().unwrap() }
                .fold(
                    onSuccess = { result -> result.map { dto -> dto.transactions.map { it.toDomain() } } },
                    onFailure = { Result.failure(it) },
                )
    }
