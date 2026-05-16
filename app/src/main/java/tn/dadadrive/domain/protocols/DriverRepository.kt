// Équivalent Swift : Domain/Repositories/DriverRepositoryProtocol.swift
package tn.dadadrive.domain.protocols

import tn.dadadrive.domain.models.ActiveRide
import tn.dadadrive.domain.models.AvailableRide
import tn.dadadrive.domain.models.CompleteRideResult
import tn.dadadrive.domain.models.DriverProfile
import tn.dadadrive.domain.models.RideOffer
import tn.dadadrive.domain.models.Vehicle
interface DriverRepository {
    suspend fun getProfile(): Result<DriverProfile>
    suspend fun createProfile(
        licenseNumber: String,
        licenseExpiry: String,
        cin: String,
        cinDeliveredAt: String,
        cinPhotoFront: String,
        cinPhotoBack: String,
        licensePhotoFront: String,
        licensePhotoBack: String
    ): Result<DriverProfile>
    suspend fun updateProfile(
        licenseNumber: String,
        licenseExpiry: String,
        cin: String,
        cinDeliveredAt: String,
        cinPhotoFront: String,
        cinPhotoBack: String,
        licensePhotoFront: String,
        licensePhotoBack: String
    ): Result<DriverProfile>
    suspend fun getVehicle(): Result<Vehicle>
    suspend fun createVehicle(
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
    ): Result<Vehicle>
    suspend fun updateVehicle(
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
    ): Result<Vehicle>
    suspend fun setOnlineStatus(isOnline: Boolean): Result<DriverProfile>
    suspend fun updateDriverLocation(lat: Double, lng: Double, headingDegrees: Double?): Result<DriverProfile>
    suspend fun getAvailableRides(): Result<List<AvailableRide>>
    suspend fun acceptRide(id: String): Result<RideOffer>
    suspend fun refuseRide(id: String): Result<Unit>
    suspend fun getMyRides(): Result<List<ActiveRide>>
    suspend fun getRide(id: String): Result<ActiveRide>
    suspend fun startRide(id: String): Result<ActiveRide>
    suspend fun completeRide(id: String): Result<CompleteRideResult>
    suspend fun cancelRide(id: String, reason: String): Result<ActiveRide>
}
