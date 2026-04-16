package com.dadadrive.data.remote.model

import com.google.gson.annotations.SerializedName

/** GET /rides/fare — backend `rideController.getFare` / `RideService.getFare`. */
data class FareApiResponse(
    val success: Boolean? = null,
    val fare: Double,
    val breakdown: FareBreakdownDto? = null
)

data class FareBreakdownDto(
    @SerializedName("base_fare") val baseFare: Double? = null,
    @SerializedName("distance_cost") val distanceCost: Double? = null,
    @SerializedName("time_cost") val timeCost: Double? = null,
    val total: Double? = null
)
