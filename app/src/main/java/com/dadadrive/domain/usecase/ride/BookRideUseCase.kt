package com.dadadrive.domain.usecase.ride

import com.dadadrive.domain.model.Location
import com.dadadrive.domain.model.Ride
import com.dadadrive.domain.repository.RideRepository
import com.dadadrive.utils.Resource
import javax.inject.Inject

class BookRideUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    suspend operator fun invoke(pickup: Location, destination: Location): Resource<Ride> {
        if (pickup.latitude == 0.0 && pickup.longitude == 0.0) {
            return Resource.Error("Point de départ invalide")
        }
        if (destination.latitude == 0.0 && destination.longitude == 0.0) {
            return Resource.Error("Destination invalide")
        }
        if (pickup.latitude == destination.latitude && pickup.longitude == destination.longitude) {
            return Resource.Error("Le départ et la destination ne peuvent pas être identiques")
        }
        return rideRepository.bookRide(pickup, destination)
    }
}
