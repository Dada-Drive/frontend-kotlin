package com.dadadrive.data.remote.dto

import com.dadadrive.domain.model.Driver
import com.dadadrive.domain.model.Location
import com.dadadrive.domain.model.Ride
import com.dadadrive.domain.model.RideStatus
import com.google.gson.annotations.SerializedName

data class LocationDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String = "",
    @SerializedName("city") val city: String = ""
) {
    fun toDomain() = Location(latitude, longitude, address, city)
}

fun Location.toDto() = LocationDto(latitude, longitude, address, city)

data class DriverDto(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("profile_picture_url") val profilePictureUrl: String? = null,
    @SerializedName("rating") val rating: Double,
    @SerializedName("total_rides") val totalRides: Int,
    @SerializedName("vehicle_make") val vehicleMake: String,
    @SerializedName("vehicle_model") val vehicleModel: String,
    @SerializedName("vehicle_color") val vehicleColor: String,
    @SerializedName("license_plate") val licensePlate: String,
    @SerializedName("current_location") val currentLocation: LocationDto,
    @SerializedName("is_available") val isAvailable: Boolean,
    @SerializedName("distance_from_passenger") val distanceFromPassenger: Double = 0.0,
    @SerializedName("estimated_arrival_minutes") val estimatedArrivalMinutes: Int = 0
) {
    fun toDomain() = Driver(
        id = id,
        fullName = fullName,
        profilePictureUrl = profilePictureUrl,
        rating = rating,
        totalRides = totalRides,
        vehicleMake = vehicleMake,
        vehicleModel = vehicleModel,
        vehicleColor = vehicleColor,
        licensePlate = licensePlate,
        currentLocation = currentLocation.toDomain(),
        isAvailable = isAvailable,
        distanceFromPassenger = distanceFromPassenger,
        estimatedArrivalMinutes = estimatedArrivalMinutes
    )
}

data class RideDto(
    @SerializedName("id") val id: String,
    @SerializedName("passenger_id") val passengerId: String,
    @SerializedName("driver_id") val driverId: String? = null,
    @SerializedName("driver") val driver: DriverDto? = null,
    @SerializedName("pickup_location") val pickupLocation: LocationDto,
    @SerializedName("destination_location") val destinationLocation: LocationDto,
    @SerializedName("status") val status: String,
    @SerializedName("estimated_price") val estimatedPrice: Double,
    @SerializedName("final_price") val finalPrice: Double? = null,
    @SerializedName("estimated_duration_minutes") val estimatedDurationMinutes: Int,
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("completed_at") val completedAt: Long? = null,
    @SerializedName("passenger_rating") val passengerRating: Int? = null,
    @SerializedName("driver_rating") val driverRating: Int? = null
) {
    fun toDomain() = Ride(
        id = id,
        passengerId = passengerId,
        driverId = driverId,
        driver = driver?.toDomain(),
        pickupLocation = pickupLocation.toDomain(),
        destinationLocation = destinationLocation.toDomain(),
        status = RideStatus.valueOf(status),
        estimatedPrice = estimatedPrice,
        finalPrice = finalPrice,
        estimatedDurationMinutes = estimatedDurationMinutes,
        distanceKm = distanceKm,
        createdAt = createdAt,
        completedAt = completedAt,
        passengerRating = passengerRating,
        driverRating = driverRating
    )
}

data class BookRideRequestDto(
    @SerializedName("pickup_location") val pickupLocation: LocationDto,
    @SerializedName("destination_location") val destinationLocation: LocationDto
)
