// Équivalent Swift : Domain/Models/DriverModels.swift
package tn.turbodrive.domain.models

data class DriverProfile(
    val id: String,
    val userId: String,
    val licenseNumber: String,
    val licenseExpiry: String,
    val isApproved: Boolean,
    val isOnline: Boolean,
    val rating: Double?,
    val totalRides: Int,
)

data class Vehicle(
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val plateNumber: String,
    val color: String,
    val vehicleType: String,
)

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
    val expiresAt: String?,
    /** Course réservée pour un passager tiers — coordonnées à contacter au point de prise en charge. */
    val pickupForOther: Boolean = false,
    val passengerName: String? = null,
    val passengerPhone: String? = null,
)

data class RideOffer(
    val id: String,
    val rideId: String,
    val driverId: String,
    val offeredFare: Double,
    val status: String,
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
    val completedAt: String?,
    val scheduledAt: String? = null,
    val expiresAt: String? = null,
    val updatedAt: String? = null,
    val pickupForOther: Boolean = false,
    val passengerName: String? = null,
    val passengerPhone: String? = null,
)

data class CompleteRideResult(
    val rideId: String,
    val commissionAmount: Double,
    val newBalance: Double,
    val warning: String?,
)

data class PassengerRideOffer(
    val id: String,
    val rideId: String,
    val driverId: String,
    val offeredFare: Double,
    val status: String,
    val driverName: String?,
    val driverPhone: String?,
    val driverRating: Double?,
    val totalRides: Int?,
    val vehicleLabel: String?,
)

data class RideRating(
    val id: String,
    val rideId: String,
    val driverId: String?,
    val score: Int,
    val comment: String?,
    val createdAt: String?,
)

data class DriverRatingsStats(
    val avgRating: Double,
    val totalRatings: Int,
)

data class NearbyTaxi(
    val id: String,
    val fullName: String?,
    val latitude: Double,
    val longitude: Double,
    val heading: Double?,
    val distanceKm: Double,
)

data class RideStop(
    val id: String,
    val rideId: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val orderIndex: Int,
    val arrivedAt: String? = null,
    val leftAt: String? = null,
    val waitMinutes: Double? = null,
)

enum class RideStatus {
    Pending,
    Scheduled,
    Offered,
    Accepted,
    InProgress,
    Completed,
    Cancelled,
}

fun RideStatus.toApiString(): String =
    when (this) {
        RideStatus.Pending -> "pending"
        RideStatus.Scheduled -> "scheduled"
        RideStatus.Offered -> "offered"
        RideStatus.Accepted -> "accepted"
        RideStatus.InProgress -> "in_progress"
        RideStatus.Completed -> "completed"
        RideStatus.Cancelled -> "cancelled"
    }

fun rideStatusFromApi(value: String): RideStatus =
    when (value) {
        "pending" -> RideStatus.Pending
        "scheduled" -> RideStatus.Scheduled
        "offered" -> RideStatus.Offered
        "accepted" -> RideStatus.Accepted
        "in_progress" -> RideStatus.InProgress
        "completed" -> RideStatus.Completed
        "cancelled" -> RideStatus.Cancelled
        else -> RideStatus.Pending
    }
