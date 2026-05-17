// Équivalent Swift : Data/Repositories/DriverRepository.swift
package tn.turbodrive.data.repositories

import tn.turbodrive.data.network.api.DriverApiService
import tn.turbodrive.data.network.envelope.unwrap
import tn.turbodrive.data.network.model.CancelRideRequestDto
import tn.turbodrive.data.network.model.CreateDriverProfileRequestDto
import tn.turbodrive.data.network.model.CreateVehicleRequestDto
import tn.turbodrive.data.network.model.SetOnlineStatusRequestDto
import tn.turbodrive.data.network.model.UpdateDriverLocationRequestDto
import tn.turbodrive.data.network.model.extractDriverRides
import tn.turbodrive.data.network.model.toDomain
import tn.turbodrive.domain.protocols.DriverRepository
import javax.inject.Inject

class DriverRepositoryImpl
    @Inject
    constructor(
        private val api: DriverApiService,
    ) : DriverRepository {
        override suspend fun getProfile() = runCatching { api.getProfile().unwrap().getOrThrow().profile.toDomain() }

        override suspend fun createProfile(
            licenseNumber: String,
            licenseExpiry: String,
            cin: String,
            cinDeliveredAt: String,
            cinPhotoFront: String,
            cinPhotoBack: String,
            licensePhotoFront: String,
            licensePhotoBack: String,
        ) = runCatching {
            api.createProfile(
                CreateDriverProfileRequestDto(
                    licenseNumber = licenseNumber,
                    licenseExpiry = licenseExpiry,
                    cin = cin,
                    cinDeliveredAt = cinDeliveredAt,
                    cinPhotoFront = cinPhotoFront,
                    cinPhotoBack = cinPhotoBack,
                    licensePhotoFront = licensePhotoFront,
                    licensePhotoBack = licensePhotoBack,
                ),
            ).unwrap().getOrThrow().profile.toDomain()
        }

        override suspend fun updateProfile(
            licenseNumber: String,
            licenseExpiry: String,
            cin: String,
            cinDeliveredAt: String,
            cinPhotoFront: String,
            cinPhotoBack: String,
            licensePhotoFront: String,
            licensePhotoBack: String,
        ) = runCatching {
            api.updateProfile(
                CreateDriverProfileRequestDto(
                    licenseNumber = licenseNumber,
                    licenseExpiry = licenseExpiry,
                    cin = cin,
                    cinDeliveredAt = cinDeliveredAt,
                    cinPhotoFront = cinPhotoFront,
                    cinPhotoBack = cinPhotoBack,
                    licensePhotoFront = licensePhotoFront,
                    licensePhotoBack = licensePhotoBack,
                ),
            ).unwrap().getOrThrow().profile.toDomain()
        }

        override suspend fun getVehicle() = runCatching { api.getVehicle().unwrap().getOrThrow().vehicle.toDomain() }

        override suspend fun createVehicle(
            make: String,
            model: String,
            year: Int,
            plateNumber: String,
            color: String,
            vehicleType: String,
            seats: Int,
            photoFront: String,
            photoSide: String,
            photoBack: String,
        ) = runCatching {
            api.createVehicle(
                CreateVehicleRequestDto(
                    make = make,
                    model = model,
                    year = year,
                    plateNumber = plateNumber,
                    color = color,
                    vehicleType = vehicleType,
                    seats = seats,
                    photoFront = photoFront,
                    photoSide = photoSide,
                    photoBack = photoBack,
                ),
            ).unwrap().getOrThrow().vehicle.toDomain()
        }

        override suspend fun updateVehicle(
            make: String,
            model: String,
            year: Int,
            plateNumber: String,
            color: String,
            vehicleType: String,
            seats: Int,
            photoFront: String,
            photoSide: String,
            photoBack: String,
        ) = runCatching {
            api.updateVehicle(
                CreateVehicleRequestDto(
                    make = make,
                    model = model,
                    year = year,
                    plateNumber = plateNumber,
                    color = color,
                    vehicleType = vehicleType,
                    seats = seats,
                    photoFront = photoFront,
                    photoSide = photoSide,
                    photoBack = photoBack,
                ),
            ).unwrap().getOrThrow().vehicle.toDomain()
        }

        override suspend fun setOnlineStatus(isOnline: Boolean) =
            runCatching {
                api.setOnlineStatus(SetOnlineStatusRequestDto(isOnline)).unwrap().getOrThrow().profile.toDomain()
            }

        override suspend fun updateDriverLocation(
            lat: Double,
            lng: Double,
            headingDegrees: Double?,
        ) = runCatching {
            api.updateLocation(
                UpdateDriverLocationRequestDto(
                    lat = lat,
                    lng = lng,
                    heading = headingDegrees,
                ),
            ).unwrap().getOrThrow().profile.toDomain()
        }

        override suspend fun getAvailableRides() = runCatching { api.getAvailableRides().unwrap().getOrThrow().rides.map { it.toDomain() } }

        override suspend fun acceptRide(id: String) = runCatching { api.acceptRide(id).unwrap().getOrThrow().offer.toDomain() }

        override suspend fun refuseRide(id: String) =
            runCatching {
                api.refuseRide(id).unwrap().getOrThrow()
                Unit
            }

        override suspend fun getMyRides() =
            runCatching {
                api.getMyRides().unwrap().getOrThrow().extractDriverRides().map { it.toDomain() }
            }

        override suspend fun getRide(id: String) = runCatching { api.getRide(id).unwrap().getOrThrow().ride.toDomain() }

        override suspend fun startRide(id: String) = runCatching { api.startRide(id).unwrap().getOrThrow().ride.toDomain() }

        override suspend fun completeRide(id: String) = runCatching { api.completeRide(id).unwrap().getOrThrow().toDomain() }

        override suspend fun cancelRide(
            id: String,
            reason: String,
        ) = runCatching {
            api.cancelRide(id, CancelRideRequestDto(reason)).unwrap().getOrThrow().ride.toDomain()
        }
    }
