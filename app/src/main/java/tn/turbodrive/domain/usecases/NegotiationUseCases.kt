package tn.turbodrive.domain.usecases

import tn.turbodrive.domain.protocols.NegotiationRepository
import javax.inject.Inject

class ProposeNegotiationUseCase
    @Inject
    constructor(
        private val repository: NegotiationRepository,
    ) {
        suspend operator fun invoke(
            rideId: String,
            proposedFare: Double,
            message: String? = null,
        ) = repository.propose(rideId, proposedFare, message)
    }

class AcceptNegotiationUseCase
    @Inject
    constructor(
        private val repository: NegotiationRepository,
    ) {
        suspend operator fun invoke(rideId: String) = repository.accept(rideId)
    }

class CounterNegotiationUseCase
    @Inject
    constructor(
        private val repository: NegotiationRepository,
    ) {
        suspend operator fun invoke(
            rideId: String,
            counterFare: Double,
            message: String? = null,
        ) = repository.counter(rideId, counterFare, message)
    }

class RejectNegotiationUseCase
    @Inject
    constructor(
        private val repository: NegotiationRepository,
    ) {
        suspend operator fun invoke(
            rideId: String,
            reason: String? = null,
        ) = repository.reject(rideId, reason)
    }
