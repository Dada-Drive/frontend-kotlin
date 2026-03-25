package com.dadadrive.domain.usecase.ride

import com.dadadrive.domain.model.Driver
import com.dadadrive.domain.model.Location
import com.dadadrive.domain.repository.RideRepository
import com.dadadrive.utils.Resource
import javax.inject.Inject

class GetNearbyDriversUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    suspend operator fun invoke(location: Location, radiusKm: Double = 5.0): Resource<List<Driver>> {
        if (location.latitude == 0.0 && location.longitude == 0.0) {
            return Resource.Error("Localisation invalide")
        }
        return rideRepository.getNearbyDrivers(location, radiusKm)
    }
}
