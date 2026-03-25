package com.dadadrive.domain.usecase.ride

import com.dadadrive.domain.model.RideStatus
import com.dadadrive.domain.repository.RideRepository
import com.dadadrive.utils.Resource
import javax.inject.Inject

class CancelRideUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    suspend operator fun invoke(rideId: String): Resource<Unit> {
        if (rideId.isBlank()) return Resource.Error("Identifiant de course invalide")

        val rideResult = rideRepository.getRideById(rideId)
        if (rideResult is Resource.Error) return rideResult

        val ride = (rideResult as Resource.Success).data
        if (ride.status == RideStatus.COMPLETED || ride.status == RideStatus.CANCELLED) {
            return Resource.Error("Cette course ne peut pas être annulée")
        }
        return rideRepository.cancelRide(rideId)
    }
}
