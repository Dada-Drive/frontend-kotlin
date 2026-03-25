package com.dadadrive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dadadrive.domain.model.Location
import com.dadadrive.domain.model.Ride
import com.dadadrive.domain.model.RideStatus

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey val id: String,
    val passengerId: String,
    val driverId: String? = null,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val pickupAddress: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val destinationAddress: String,
    val status: String,
    val estimatedPrice: Double,
    val finalPrice: Double? = null,
    val estimatedDurationMinutes: Int,
    val distanceKm: Double,
    val createdAt: Long,
    val completedAt: Long? = null,
    val passengerRating: Int? = null,
    val driverRating: Int? = null
) {
    fun toDomain() = Ride(
        id = id,
        passengerId = passengerId,
        driverId = driverId,
        driver = null,
        pickupLocation = Location(pickupLatitude, pickupLongitude, pickupAddress),
        destinationLocation = Location(destinationLatitude, destinationLongitude, destinationAddress),
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

fun Ride.toEntity() = RideEntity(
    id = id,
    passengerId = passengerId,
    driverId = driverId,
    pickupLatitude = pickupLocation.latitude,
    pickupLongitude = pickupLocation.longitude,
    pickupAddress = pickupLocation.address,
    destinationLatitude = destinationLocation.latitude,
    destinationLongitude = destinationLocation.longitude,
    destinationAddress = destinationLocation.address,
    status = status.name,
    estimatedPrice = estimatedPrice,
    finalPrice = finalPrice,
    estimatedDurationMinutes = estimatedDurationMinutes,
    distanceKm = distanceKm,
    createdAt = createdAt,
    completedAt = completedAt,
    passengerRating = passengerRating,
    driverRating = driverRating
)
