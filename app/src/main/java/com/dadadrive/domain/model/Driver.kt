package com.dadadrive.domain.model

data class Driver(
    val id: String,
    val fullName: String,
    val profilePictureUrl: String? = null,
    val rating: Double,
    val totalRides: Int,
    val vehicleMake: String,
    val vehicleModel: String,
    val vehicleColor: String,
    val licensePlate: String,
    val currentLocation: Location,
    val isAvailable: Boolean,
    val distanceFromPassenger: Double = 0.0,
    val estimatedArrivalMinutes: Int = 0
)
