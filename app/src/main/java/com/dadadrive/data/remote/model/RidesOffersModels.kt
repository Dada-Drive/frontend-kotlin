package com.dadadrive.data.remote.model

import com.dadadrive.domain.model.PassengerRideOffer
import com.google.gson.annotations.SerializedName

data class RideOffersResponseDto(
    val success: Boolean? = null,
    val offers: List<PassengerRideOfferDto>
)

data class PassengerRideOfferDto(
    val id: String,
    @SerializedName("ride_id") val rideId: String,
    @SerializedName("driver_id") val driverId: String,
    @SerializedName("offered_fare") val offeredFare: Double,
    val status: String,
    @SerializedName("driver_name") val driverName: String?,
    @SerializedName("driver_phone") val driverPhone: String?,
    val rating: Double?,
    @SerializedName("total_rides") val totalRides: Int?,
    val make: String?,
    val model: String?,
    val year: Int?
)

fun PassengerRideOfferDto.toDomain() = PassengerRideOffer(
    id = id,
    rideId = rideId,
    driverId = driverId,
    offeredFare = offeredFare,
    status = status,
    driverName = driverName,
    driverPhone = driverPhone,
    driverRating = rating,
    totalRides = totalRides,
    vehicleLabel = listOfNotNull(make, model).joinToString(" ").ifBlank {
        year?.toString()
    }
)
