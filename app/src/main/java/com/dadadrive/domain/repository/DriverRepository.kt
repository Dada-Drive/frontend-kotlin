// Équivalent Swift : Domain/Repositories/DriverRepositoryProtocol.swift
package com.dadadrive.domain.repository

import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.AvailableRide
import com.dadadrive.domain.model.CompleteRideResult
import com.dadadrive.domain.model.DriverProfile
import com.dadadrive.domain.model.RideOffer
import com.dadadrive.domain.model.Vehicle
import com.dadadrive.domain.model.VehicleType

interface DriverRepository {
    suspend fun getProfile(): Result<DriverProfile>
    suspend fun createProfile(licenseNumber: String, licenseExpiry: String): Result<DriverProfile>
    suspend fun updateProfile(licenseNumber: String, licenseExpiry: String): Result<DriverProfile>
    suspend fun getVehicle(): Result<Vehicle>
    suspend fun createVehicle(
        make: String,
        model: String,
        year: Int,
        plateNumber: String,
        color: String,
        vehicleType: VehicleType
    ): Result<Vehicle>
    suspend fun updateVehicle(
        make: String,
        model: String,
        year: Int,
        plateNumber: String,
        color: String,
        vehicleType: VehicleType
    ): Result<Vehicle>
    suspend fun setOnlineStatus(isOnline: Boolean): Result<DriverProfile>
    suspend fun getAvailableRides(): Result<List<AvailableRide>>
    suspend fun acceptRide(id: String): Result<RideOffer>
    suspend fun refuseRide(id: String): Result<Unit>
    suspend fun getMyRides(): Result<List<ActiveRide>>
    suspend fun getRide(id: String): Result<ActiveRide>
    suspend fun startRide(id: String): Result<ActiveRide>
    suspend fun completeRide(id: String): Result<CompleteRideResult>
    suspend fun cancelRide(id: String, reason: String): Result<ActiveRide>
}
