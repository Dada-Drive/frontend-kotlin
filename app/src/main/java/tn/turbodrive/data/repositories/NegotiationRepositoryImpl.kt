package tn.turbodrive.data.repositories

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tn.turbodrive.data.network.model.NegotiateAcceptDtoRequest
import tn.turbodrive.data.network.model.NegotiateCounterDtoRequest
import tn.turbodrive.data.network.model.NegotiateProposeDtoRequest
import tn.turbodrive.data.network.model.NegotiateRejectDtoRequest
import tn.turbodrive.data.socket.SocketEventManager
import tn.turbodrive.domain.protocols.NegotiationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NegotiationRepositoryImpl
    @Inject
    constructor(
        private val socketEventManager: SocketEventManager,
    ) : NegotiationRepository {
        private val json = Json { encodeDefaults = false }

        override suspend fun propose(
            rideId: String,
            proposedFare: Double,
            message: String?,
        ): Result<Unit> =
            runCatching {
                socketEventManager.emit(
                    "negotiate:propose",
                    json.encodeToString(NegotiateProposeDtoRequest(rideId, proposedFare, message)),
                )
            }

        override suspend fun accept(rideId: String): Result<Unit> =
            runCatching {
                socketEventManager.emit(
                    "negotiate:accept",
                    json.encodeToString(NegotiateAcceptDtoRequest(rideId)),
                )
            }

        override suspend fun counter(
            rideId: String,
            counterFare: Double,
            message: String?,
        ): Result<Unit> =
            runCatching {
                socketEventManager.emit(
                    "negotiate:counter",
                    json.encodeToString(NegotiateCounterDtoRequest(rideId, counterFare, message)),
                )
            }

        override suspend fun reject(
            rideId: String,
            reason: String?,
        ): Result<Unit> =
            runCatching {
                socketEventManager.emit(
                    "negotiate:reject",
                    json.encodeToString(NegotiateRejectDtoRequest(rideId, reason)),
                )
            }
    }
