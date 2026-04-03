// Équivalent Swift : Domain/Models/DriverModels.swift
package com.dadadrive.domain.model

data class DriverProfile(
    val id: String,
    val userId: String,
    val licenseNumber: String,
    val licenseExpiry: String,
    val isApproved: Boolean,
    val isOnline: Boolean,
    val rating: Double?,
    val totalRides: Int
)

data class Vehicle(
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val plateNumber: String,
    val color: String,
    val vehicleType: String
)

enum class VehicleType(val rawValue: String) {
    Economy("economy"),
    Comfort("comfort"),
    Xl("xl"),
    Van("van")
}

data class AvailableRide(
    val id: String,
    val riderName: String?,
    val riderPhone: String?,
    val pickupAddress: String,
    val dropoffAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val calculatedFare: Double,
    val vehicleType: String?,
    val status: RideStatus,
    val expiresAt: String?
)

data class RideOffer(
    val id: String,
    val rideId: String,
    val driverId: String,
    val offeredFare: Double,
    val status: String
)

data class ActiveRide(
    val id: String,
    val riderName: String?,
    val riderPhone: String?,
    val pickupAddress: String,
    val dropoffAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val finalFare: Double?,
    val calculatedFare: Double,
    val status: RideStatus,
    val startedAt: String?,
    val completedAt: String?
)

data class CompleteRideResult(
    val rideId: String,
    val commissionAmount: Double,
    val newBalance: Double,
    val warning: String?
)

enum class RideStatus {
    Pending,
    Offered,
    Accepted,
    InProgress,
    Completed,
    Cancelled
}

fun RideStatus.toApiString(): String = when (this) {
    RideStatus.Pending -> "pending"
    RideStatus.Offered -> "offered"
    RideStatus.Accepted -> "accepted"
    RideStatus.InProgress -> "in_progress"
    RideStatus.Completed -> "completed"
    RideStatus.Cancelled -> "cancelled"
}

fun rideStatusFromApi(value: String): RideStatus = when (value) {
    "pending" -> RideStatus.Pending
    "offered" -> RideStatus.Offered
    "accepted" -> RideStatus.Accepted
    "in_progress" -> RideStatus.InProgress
    "completed" -> RideStatus.Completed
    "cancelled" -> RideStatus.Cancelled
    else -> RideStatus.Pending
}
