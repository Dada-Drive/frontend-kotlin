package tn.turbodrive.domain.protocols

interface NegotiationRepository {
    suspend fun propose(
        rideId: String,
        proposedFare: Double,
        message: String? = null,
    ): Result<Unit>

    suspend fun accept(rideId: String): Result<Unit>

    suspend fun counter(
        rideId: String,
        counterFare: Double,
        message: String? = null,
    ): Result<Unit>

    suspend fun reject(
        rideId: String,
        reason: String? = null,
    ): Result<Unit>
}
