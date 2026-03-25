package com.dadadrive.domain.usecase.ride

import com.dadadrive.domain.model.Ride
import com.dadadrive.domain.repository.RideRepository
import com.dadadrive.utils.Resource
import javax.inject.Inject

class GetRideHistoryUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    suspend operator fun invoke(page: Int = 0, limit: Int = 20): Resource<List<Ride>> {
        return rideRepository.getRideHistory(page, limit)
    }
}
