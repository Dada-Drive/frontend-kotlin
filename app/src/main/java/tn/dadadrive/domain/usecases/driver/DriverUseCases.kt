// Équivalent Swift : Domain/UseCases/DriverUseCases.swift
package tn.dadadrive.domain.usecases.driver

import tn.dadadrive.domain.protocols.DriverRepository
import javax.inject.Inject

class CreateDriverProfileUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(
        licenseNumber: String,
        licenseExpiry: String,
        cin: String,
        cinDeliveredAt: String,
        cinPhotoFront: String,
        cinPhotoBack: String,
        licensePhotoFront: String,
        licensePhotoBack: String
    ) = repository.createProfile(
        licenseNumber, licenseExpiry,
        cin, cinDeliveredAt,
        cinPhotoFront, cinPhotoBack,
        licensePhotoFront, licensePhotoBack
    )
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
        vehicleType: String,
        seats: Int,
        photoFront: String,
        photoSide: String,
        photoBack: String
    ) = repository.createVehicle(make, model, year, plateNumber, color, vehicleType, seats, photoFront, photoSide, photoBack)
}

class SetOnlineStatusUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(isOnline: Boolean) = repository.setOnlineStatus(isOnline)
}

class UpdateDriverLocationUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(lat: Double, lng: Double, headingDegrees: Double?) =
        repository.updateDriverLocation(lat, lng, headingDegrees)
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
