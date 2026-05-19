package tn.turbodrive.domain.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tn.turbodrive.domain.models.ResyncResult
import tn.turbodrive.domain.models.RideStatus
import tn.turbodrive.domain.protocols.RidesRepository
import tn.turbodrive.domain.protocols.WalletRepository
import javax.inject.Inject

class ResyncOnReconnectUseCase
    @Inject
    constructor(
        private val ridesRepository: RidesRepository,
        private val walletRepository: WalletRepository,
    ) {
        suspend operator fun invoke(): ResyncResult =
            coroutineScope {
                val ridesDeferred = async { ridesRepository.getMyRides() }
                val walletDeferred = async { walletRepository.getWallet() }

                val rides = ridesDeferred.await().getOrNull()
                val wallet = walletDeferred.await().getOrNull()

                val activeRide =
                    rides?.firstOrNull {
                        it.status == RideStatus.Accepted || it.status == RideStatus.InProgress
                    }

                ResyncResult(activeRide = activeRide, walletInfo = wallet)
            }
    }
