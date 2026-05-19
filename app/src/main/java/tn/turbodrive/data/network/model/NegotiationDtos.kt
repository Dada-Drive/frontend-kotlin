package tn.turbodrive.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NegotiateProposeDtoRequest(
    val rideId: String,
    val proposedFare: Double,
    val message: String? = null,
)

@Serializable
data class NegotiateCounterDtoRequest(
    val rideId: String,
    val counterFare: Double,
    val message: String? = null,
)

@Serializable
data class NegotiateAcceptDtoRequest(val rideId: String)

@Serializable
data class NegotiateRejectDtoRequest(
    val rideId: String,
    val reason: String? = null,
)
