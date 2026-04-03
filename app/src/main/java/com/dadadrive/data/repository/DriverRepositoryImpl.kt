// Équivalent Swift : Data/Repositories/DriverRepository.swift
package com.dadadrive.data.repository

import com.dadadrive.data.remote.api.DriverApiService
import com.dadadrive.data.remote.model.CancelRideRequestDto
import com.dadadrive.data.remote.model.CreateDriverProfileRequestDto
import com.dadadrive.data.remote.model.CreateVehicleRequestDto
import com.dadadrive.data.remote.model.SetOnlineStatusRequestDto
import com.dadadrive.data.remote.model.toDomain
import com.dadadrive.domain.model.VehicleType
import com.dadadrive.domain.repository.DriverRepository
import javax.inject.Inject

class DriverRepositoryImpl @Inject constructor(
    private val api: DriverApiService
) : DriverRepository {

    override suspend fun getProfile() = runCatching {
        api.getProfile().profile.toDomain()
    }

    override suspend fun createProfile(licenseNumber: String, licenseExpiry: String) = runCatching {
        api.createProfile(
            CreateDriverProfileRequestDto(licenseNumber, licenseExpiry)
        ).profile.toDomain()
    }

    override suspend fun updateProfile(licenseNumber: String, licenseExpiry: String) = runCatching {
        api.updateProfile(
            CreateDriverProfileRequestDto(licenseNumber, licenseExpiry)
        ).profile.toDomain()
    }

    override suspend fun getVehicle() = runCatching {
        api.getVehicle().vehicle.toDomain()
    }

    override suspend fun createVehicle(
        make: String,
        model: String,
        year: Int,
        plateNumber: String,
        color: String,
        vehicleType: VehicleType
    ) = runCatching {
        api.createVehicle(
            CreateVehicleRequestDto(
                make = make,
                model = model,
                year = year,
                plateNumber = plateNumber,
                color = color,
                vehicleType = vehicleType.rawValue
            )
        ).vehicle.toDomain()
    }

    override suspend fun updateVehicle(
        make: String,
        model: String,
        year: Int,
        plateNumber: String,
        color: String,
        vehicleType: VehicleType
    ) = runCatching {
        api.updateVehicle(
            CreateVehicleRequestDto(
                make = make,
                model = model,
                year = year,
                plateNumber = plateNumber,
                color = color,
                vehicleType = vehicleType.rawValue
            )
        ).vehicle.toDomain()
    }

    override suspend fun setOnlineStatus(isOnline: Boolean) = runCatching {
        api.setOnlineStatus(SetOnlineStatusRequestDto(isOnline)).profile.toDomain()
    }

    override suspend fun getAvailableRides() = runCatching {
        api.getAvailableRides().rides.map { it.toDomain() }
    }

    override suspend fun acceptRide(id: String) = runCatching {
        api.acceptRide(id).offer.toDomain()
    }

    override suspend fun refuseRide(id: String) = runCatching {
        api.refuseRide(id)
        Unit
    }

    override suspend fun getMyRides() = runCatching {
        api.getMyRides().rides.map { it.toDomain() }
    }

    override suspend fun getRide(id: String) = runCatching {
        api.getRide(id).ride.toDomain()
    }

    override suspend fun startRide(id: String) = runCatching {
        api.startRide(id).ride.toDomain()
    }

    override suspend fun completeRide(id: String) = runCatching {
        api.completeRide(id).toDomain()
    }

    override suspend fun cancelRide(id: String, reason: String) = runCatching {
        api.cancelRide(id, CancelRideRequestDto(reason)).ride.toDomain()
    }
}
