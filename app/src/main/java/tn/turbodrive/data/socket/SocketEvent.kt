package tn.turbodrive.data.socket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationUpdatePayload(
    val lat: Double,
    val lng: Double,
)

@Serializable
data class DriverStatusPayload(
    @SerialName("isOnline") val isOnline: Boolean,
)

@Serializable
data class RideNewRequestPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("pickupLat") val pickupLat: Double,
    @SerialName("pickupLng") val pickupLng: Double,
    @SerialName("pickupAddress") val pickupAddress: String,
    @SerialName("dropoffAddress") val dropoffAddress: String,
    @SerialName("vehicleType") val vehicleType: String,
    @SerialName("calculatedFare") val calculatedFare: Double,
    @SerialName("riderName") val riderName: String,
)

@Serializable
data class RideNewOfferPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("offerId") val offerId: String,
    @SerialName("driverId") val driverId: String,
    @SerialName("driverName") val driverName: String,
    @SerialName("driverRating") val driverRating: Double,
    @SerialName("vehicleType") val vehicleType: String,
    @SerialName("offeredFare") val offeredFare: Double,
)

@Serializable
data class RideAcceptedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("riderId") val riderId: String,
    @SerialName("riderName") val riderName: String,
    @SerialName("pickupLat") val pickupLat: Double,
    @SerialName("pickupLng") val pickupLng: Double,
    @SerialName("pickupAddress") val pickupAddress: String,
    @SerialName("dropoffAddress") val dropoffAddress: String,
)

@Serializable
data class RideOfferRejectedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("offerId") val offerId: String,
)

@Serializable
data class RideDriverArrivedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("arrivedAt") val arrivedAt: String,
)

@Serializable
data class RideStatusChangedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("status") val status: String,
    @SerialName("timestamp") val timestamp: String,
)

@Serializable
data class RideCompletedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("status") val status: String,
    @SerialName("completedAt") val completedAt: String,
)

@Serializable
data class RideCancelledPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("cancelledBy") val cancelledBy: String,
    @SerialName("cancelReason") val cancelReason: String? = null,
)

@Serializable
data class RideDriverLocationPayload(
    @SerialName("driverId") val driverId: String,
    val lat: Double,
    val lng: Double,
    @SerialName("timestamp") val timestamp: String,
)

sealed class SocketClientEvent {
    abstract val eventName: String

    data class LocationUpdate(val payload: LocationUpdatePayload) : SocketClientEvent() {
        override val eventName: String = "location:update"
    }

    data class DriverStatus(val payload: DriverStatusPayload) : SocketClientEvent() {
        override val eventName: String = "driver:status"
    }
}
