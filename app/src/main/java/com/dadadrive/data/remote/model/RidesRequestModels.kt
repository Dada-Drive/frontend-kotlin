package com.dadadrive.data.remote.model

import com.google.gson.annotations.SerializedName

data class RequestRideRequestDto(
    @SerializedName("pickup_lat") val pickupLat: Double,
    @SerializedName("pickup_lng") val pickupLng: Double,
    @SerializedName("pickup_address") val pickupAddress: String,
    @SerializedName("dropoff_lat") val dropoffLat: Double,
    @SerializedName("dropoff_lng") val dropoffLng: Double,
    @SerializedName("dropoff_address") val dropoffAddress: String,
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("estimated_minutes") val estimatedMinutes: Int,
    @SerializedName("vehicle_type") val vehicleType: String = "economy",
    @SerializedName("scheduled_at") val scheduledAt: String? = null,
    @SerializedName("is_for_someone_else") val pickupForOther: Boolean = false,
    @SerializedName("passenger_name") val passengerName: String? = null,
    @SerializedName("passenger_phone") val passengerPhone: String? = null
)

data class CancelRideBodyDto(
    val reason: String? = null
)
