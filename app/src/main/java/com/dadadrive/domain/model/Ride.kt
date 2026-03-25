package com.dadadrive.domain.model

data class Ride(
    val id: String,
    val passengerId: String,
    val driverId: String? = null,
    val driver: Driver? = null,
    val pickupLocation: Location,
    val destinationLocation: Location,
    val status: RideStatus,
    val estimatedPrice: Double,
    val finalPrice: Double? = null,
    val estimatedDurationMinutes: Int,
    val distanceKm: Double,
    val createdAt: Long,
    val completedAt: Long? = null,
    val passengerRating: Int? = null,
    val driverRating: Int? = null
)

enum class RideStatus {
    SEARCHING,
    DRIVER_FOUND,
    DRIVER_EN_ROUTE,
    DRIVER_ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
