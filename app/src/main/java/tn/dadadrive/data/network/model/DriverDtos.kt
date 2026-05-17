// Équivalent Swift : Data/DTOs/DriverDTOs.swift (+ parsing vers domain)
package tn.dadadrive.data.network.model

import com.google.gson.annotations.SerializedName
import tn.dadadrive.domain.models.ActiveRide
import tn.dadadrive.domain.models.AvailableRide
import tn.dadadrive.domain.models.CompleteRideResult
import tn.dadadrive.domain.models.DriverProfile
import tn.dadadrive.domain.models.RideOffer
import tn.dadadrive.domain.models.Vehicle
import tn.dadadrive.domain.models.rideStatusFromApi

data class DriverProfileResponseDto(
    val profile: DriverProfileDto,
)

data class DriverProfileDto(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("license_number") val licenseNumber: String,
    @SerializedName("license_expiry") val licenseExpiry: String,
    @SerializedName("is_approved") val isApproved: Boolean,
    @SerializedName("is_online") val isOnline: Boolean,
    val rating: Double?,
    @SerializedName("total_rides") val totalRides: Int,
)

fun DriverProfileDto.toDomain() =
    DriverProfile(
        id = id,
        userId = userId,
        licenseNumber = licenseNumber,
        licenseExpiry = licenseExpiry,
        isApproved = isApproved,
        isOnline = isOnline,
        rating = rating,
        totalRides = totalRides,
    )

data class CreateDriverProfileRequestDto(
    @SerializedName("license_number") val licenseNumber: String,
    @SerializedName("license_expiry") val licenseExpiry: String,
    @SerializedName("cin") val cin: String,
    @SerializedName("cin_delivered_at") val cinDeliveredAt: String,
    @SerializedName("cin_photo_front") val cinPhotoFront: String,
    @SerializedName("cin_photo_back") val cinPhotoBack: String,
    @SerializedName("license_photo_front") val licensePhotoFront: String,
    @SerializedName("license_photo_back") val licensePhotoBack: String,
)

data class SetOnlineStatusRequestDto(
    @SerializedName("is_online") val isOnline: Boolean,
)

data class UpdateDriverLocationRequestDto(
    val lat: Double,
    val lng: Double,
    val heading: Double? = null,
)

data class VehicleResponseDto(
    val vehicle: VehicleDto,
)

data class VehicleDto(
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    @SerializedName("plate_number") val plateNumber: String,
    val color: String,
    @SerializedName("vehicle_type") val vehicleType: String,
)

fun VehicleDto.toDomain() =
    Vehicle(
        id = id,
        make = make,
        model = model,
        year = year,
        plateNumber = plateNumber,
        color = color,
        vehicleType = vehicleType,
    )

data class CreateVehicleRequestDto(
    val make: String,
    val model: String,
    val year: Int,
    @SerializedName("plate_number") val plateNumber: String,
    val color: String,
    @SerializedName("vehicle_type") val vehicleType: String,
    val seats: Int,
    @SerializedName("photo_front") val photoFront: String,
    @SerializedName("photo_side") val photoSide: String,
    @SerializedName("photo_back") val photoBack: String,
)

data class AvailableRidesResponseDto(
    val rides: List<AvailableRideDto>,
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
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName(
        value = "is_for_someone_else",
        alternate = ["pickup_for_other", "isForSomeoneElse", "pickupForOther"],
    ) val pickupForOther: Boolean? = null,
    @SerializedName(value = "passenger_name", alternate = ["passengerName"]) val passengerName: String? = null,
    @SerializedName(value = "passenger_phone", alternate = ["passengerPhone"]) val passengerPhone: String? = null,
)

fun AvailableRideDto.toDomain() =
    AvailableRide(
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
        expiresAt = expiresAt,
        pickupForOther = pickupForOther == true,
        passengerName = passengerName,
        passengerPhone = passengerPhone,
    )

data class RideOfferResponseDto(
    val offer: RideOfferDto,
)

data class RideOfferDto(
    val id: String,
    @SerializedName("ride_id") val rideId: String,
    @SerializedName("driver_id") val driverId: String,
    @SerializedName("offered_fare") val offeredFare: Double,
    val status: String,
)

fun RideOfferDto.toDomain() =
    RideOffer(
        id = id,
        rideId = rideId,
        driverId = driverId,
        offeredFare = offeredFare,
        status = status,
    )

data class RideResponseDto(
    val ride: ActiveRideDto,
)

data class RidesResponseDto(
    val rides: DriverRidesPayloadDto? = null,
    @SerializedName("driverRides") val driverRides: List<ActiveRideDto>? = null,
)

data class DriverRidesPayloadDto(
    @SerializedName("driverRides") val driverRides: List<ActiveRideDto>? = null,
    @SerializedName("riderRides") val riderRides: List<ActiveRideDto>? = null,
)

fun RidesResponseDto.extractDriverRides(): List<ActiveRideDto> {
    return when {
        !driverRides.isNullOrEmpty() -> driverRides
        !rides?.driverRides.isNullOrEmpty() -> rides?.driverRides.orEmpty()
        else -> emptyList()
    }
}

fun RidesResponseDto.extractRiderRides(): List<ActiveRideDto> {
    return when {
        !rides?.riderRides.isNullOrEmpty() -> rides?.riderRides.orEmpty()
        !driverRides.isNullOrEmpty() -> driverRides
        !rides?.driverRides.isNullOrEmpty() -> rides?.driverRides.orEmpty()
        else -> emptyList()
    }
}

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
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("scheduled_at") val scheduledAt: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName(
        value = "is_for_someone_else",
        alternate = ["pickup_for_other", "isForSomeoneElse", "pickupForOther"],
    ) val pickupForOther: Boolean? = null,
    @SerializedName(value = "passenger_name", alternate = ["passengerName"]) val passengerName: String? = null,
    @SerializedName(value = "passenger_phone", alternate = ["passengerPhone"]) val passengerPhone: String? = null,
)

fun ActiveRideDto.toDomain() =
    ActiveRide(
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
        completedAt = completedAt,
        scheduledAt = scheduledAt,
        expiresAt = expiresAt,
        updatedAt = updatedAt,
        pickupForOther = pickupForOther == true,
        passengerName = passengerName,
        passengerPhone = passengerPhone,
    )

data class CompleteRideResponseDto(
    @SerializedName("rideId") val rideId: String,
    @SerializedName("commission_amount") val commissionAmount: Double,
    @SerializedName("newBalance") val newBalance: Double,
    val warning: String?,
)

fun CompleteRideResponseDto.toDomain() =
    CompleteRideResult(
        rideId = rideId,
        commissionAmount = commissionAmount,
        newBalance = newBalance,
        warning = warning,
    )

/** Body optionnel côté API (`cancelRide` accepte `reason` absent). */
data class CancelRideRequestDto(val reason: String? = null)

data class MessageResponseDto(
    val message: String?,
)
