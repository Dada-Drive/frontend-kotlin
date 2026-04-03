// Équivalent Swift : Domain/UseCases/DriverUseCases.swift
package com.dadadrive.domain.usecase.driver

import com.dadadrive.domain.model.VehicleType
import com.dadadrive.domain.repository.DriverRepository
import javax.inject.Inject

class CreateDriverProfileUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(licenseNumber: String, licenseExpiry: String) =
        repository.createProfile(licenseNumber, licenseExpiry)
}

class CreateVehicleUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(
        make: String,
        model: String,
        year: Int,
        plateNumber: String,
        color: String,
        vehicleType: VehicleType
    ) = repository.createVehicle(make, model, year, plateNumber, color, vehicleType)
}

class SetOnlineStatusUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(isOnline: Boolean) = repository.setOnlineStatus(isOnline)
}

class GetAvailableRidesUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke() = repository.getAvailableRides()
}

class AcceptRideUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(rideId: String) = repository.acceptRide(rideId)
}

class RefuseRideUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(rideId: String) = repository.refuseRide(rideId)
}

class GetMyRidesUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke() = repository.getMyRides()
}

class StartRideUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(rideId: String) = repository.startRide(rideId)
}

class CompleteRideUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(rideId: String) = repository.completeRide(rideId)
}

class CancelRideUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(rideId: String, reason: String) =
        repository.cancelRide(rideId, reason)
}
