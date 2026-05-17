package tn.turbodrive.data.repositories

import tn.turbodrive.data.network.api.WalletApiService
import tn.turbodrive.data.network.envelope.unwrap
import tn.turbodrive.data.network.model.toDomain
import tn.turbodrive.domain.protocols.WalletRepository
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
