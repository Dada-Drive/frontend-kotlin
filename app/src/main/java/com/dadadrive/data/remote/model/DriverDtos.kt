// Équivalent Swift : Data/DTOs/DriverDTOs.swift (+ parsing vers domain)
package com.dadadrive.data.remote.model

import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.AvailableRide
import com.dadadrive.domain.model.CompleteRideResult
import com.dadadrive.domain.model.DriverProfile
import com.dadadrive.domain.model.RideOffer
import com.dadadrive.domain.model.Vehicle
import com.dadadrive.domain.model.rideStatusFromApi
import com.google.gson.annotations.SerializedName

data class DriverProfileResponseDto(
    val success: Boolean? = null,
    val profile: DriverProfileDto
)

data class DriverProfileDto(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("license_number") val licenseNumber: String,
    @SerializedName("license_expiry") val licenseExpiry: String,
    @SerializedName("is_approved") val isApproved: Boolean,
    @SerializedName("is_online") val isOnline: Boolean,
    val rating: Double?,
    @SerializedName("total_rides") val totalRides: Int
)

fun DriverProfileDto.toDomain() = DriverProfile(
    id = id,
    userId = userId,
    licenseNumber = licenseNumber,
    licenseExpiry = licenseExpiry,
    isApproved = isApproved,
    isOnline = isOnline,
    rating = rating,
    totalRides = totalRides
)

data class CreateDriverProfileRequestDto(
    @SerializedName("license_number") val licenseNumber: String,
    @SerializedName("license_expiry") val licenseExpiry: String
)

data class SetOnlineStatusRequestDto(
    @SerializedName("is_online") val isOnline: Boolean
)

data class VehicleResponseDto(
    val success: Boolean? = null,
    val vehicle: VehicleDto
)

data class VehicleDto(
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    @SerializedName("plate_number") val plateNumber: String,
    val color: String,
    @SerializedName("vehicle_type") val vehicleType: String
)

fun VehicleDto.toDomain() = Vehicle(
    id = id,
    make = make,
    model = model,
    year = year,
    plateNumber = plateNumber,
    color = color,
    vehicleType = vehicleType
)

data class CreateVehicleRequestDto(
    val make: String,
    val model: String,
    val year: Int,
    @SerializedName("plate_number") val plateNumber: String,
    val color: String,
    @SerializedName("vehicle_type") val vehicleType: String
)

data class AvailableRidesResponseDto(
    val success: Boolean? = null,
    val rides: List<AvailableRideDto>
)

data class AvailableRideDto(
    val id: String,
    @SerializedName("rider_name") val riderName: String?,
    @SerializedName("rider_phone") val riderPhone: String?,
    @SerializedName("pickup_address") val pickupAddress: String,
    @SerializedName("dropoff_address") val dropoffAddress: String,
    @SerializedName("pickup_lat") val pickupLat: Double,
    @SerializedName("pickup_lng") val pickupLng: Double,
    @SerializedName("dropoff_lat") val dropoffLat: Double,
    @SerializedName("dropoff_lng") val dropoffLng: Double,
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("estimated_minutes") val estimatedMinutes: Int,
    @SerializedName("calculated_fare") val calculatedFare: Double,
    @SerializedName("vehicle_type") val vehicleType: String?,
    val status: String,
    @SerializedName("expires_at") val expiresAt: String?
)

fun AvailableRideDto.toDomain() = AvailableRide(
    id = id,
    riderName = riderName,
    riderPhone = riderPhone,
    pickupAddress = pickupAddress,
    dropoffAddress = dropoffAddress,
    pickupLat = pickupLat,
    pickupLng = pickupLng,
    dropoffLat = dropoffLat,
    dropoffLng = dropoffLng,
    distanceKm = distanceKm,
    estimatedMinutes = estimatedMinutes,
    calculatedFare = calculatedFare,
    vehicleType = vehicleType,
    status = rideStatusFromApi(status),
    expiresAt = expiresAt
)

data class RideOfferResponseDto(
    val success: Boolean? = null,
    val offer: RideOfferDto
)

data class RideOfferDto(
    val id: String,
    @SerializedName("ride_id") val rideId: String,
    @SerializedName("driver_id") val driverId: String,
    @SerializedName("offered_fare") val offeredFare: Double,
    val status: String
)

fun RideOfferDto.toDomain() = RideOffer(
    id = id,
    rideId = rideId,
    driverId = driverId,
    offeredFare = offeredFare,
    status = status
)

data class RideResponseDto(
    val success: Boolean? = null,
    val ride: ActiveRideDto
)

data class RidesResponseDto(
    val success: Boolean? = null,
    val rides: List<ActiveRideDto>
)

data class ActiveRideDto(
    val id: String,
    @SerializedName("rider_name") val riderName: String?,
    @SerializedName("rider_phone") val riderPhone: String?,
    @SerializedName("pickup_address") val pickupAddress: String,
    @SerializedName("dropoff_address") val dropoffAddress: String,
    @SerializedName("pickup_lat") val pickupLat: Double,
    @SerializedName("pickup_lng") val pickupLng: Double,
    @SerializedName("dropoff_lat") val dropoffLat: Double,
    @SerializedName("dropoff_lng") val dropoffLng: Double,
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("estimated_minutes") val estimatedMinutes: Int,
    @SerializedName("final_fare") val finalFare: Double?,
    @SerializedName("calculated_fare") val calculatedFare: Double,
    val status: String,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("completed_at") val completedAt: String?
)

fun ActiveRideDto.toDomain() = ActiveRide(
    id = id,
    riderName = riderName,
    riderPhone = riderPhone,
    pickupAddress = pickupAddress,
    dropoffAddress = dropoffAddress,
    pickupLat = pickupLat,
    pickupLng = pickupLng,
    dropoffLat = dropoffLat,
    dropoffLng = dropoffLng,
    distanceKm = distanceKm,
    estimatedMinutes = estimatedMinutes,
    finalFare = finalFare,
    calculatedFare = calculatedFare,
    status = rideStatusFromApi(status),
    startedAt = startedAt,
    completedAt = completedAt
)

data class CompleteRideResponseDto(
    val success: Boolean? = null,
    @SerializedName("rideId") val rideId: String,
    @SerializedName("commission_amount") val commissionAmount: Double,
    @SerializedName("newBalance") val newBalance: Double,
    val warning: String?
)

fun CompleteRideResponseDto.toDomain() = CompleteRideResult(
    rideId = rideId,
    commissionAmount = commissionAmount,
    newBalance = newBalance,
    warning = warning
)

/** Body optionnel côté API (`cancelRide` accepte `reason` absent). */
data class CancelRideRequestDto(val reason: String? = null)

data class MessageResponseDto(
    val success: Boolean? = null,
    val message: String?
)
