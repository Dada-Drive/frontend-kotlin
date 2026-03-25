package com.dadadrive.domain.repository

import com.dadadrive.domain.model.Driver
import com.dadadrive.domain.model.Location
import com.dadadrive.domain.model.Ride
import com.dadadrive.utils.Resource
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    suspend fun getNearbyDrivers(location: Location, radiusKm: Double = 5.0): Resource<List<Driver>>
    suspend fun estimateRidePrice(pickup: Location, destination: Location): Resource<Double>
    suspend fun bookRide(pickup: Location, destination: Location): Resource<Ride>
    suspend fun cancelRide(rideId: String): Resource<Unit>
    suspend fun getRideById(rideId: String): Resource<Ride>
    suspend fun getRideHistory(page: Int = 0, limit: Int = 20): Resource<List<Ride>>
    fun observeRideStatus(rideId: String): Flow<Ride>
}
