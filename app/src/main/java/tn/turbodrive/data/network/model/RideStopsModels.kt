package tn.turbodrive.data.network.model

import com.google.gson.annotations.SerializedName
import tn.turbodrive.domain.models.RideStop

/**
 * Shape matches backend `POST /rides/:id/stops` contract:
 * `body.stops[].address`, `body.stops[].lat`, `body.stops[].lng`.
 */
data class AddRideStopsRequestDto(
    @SerializedName("stops") val stops: List<RideStopRequestItemDto>,
)

data class RideStopRequestItemDto(
    @SerializedName("address") val address: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
)

/** Row returned by the backend after stops are stored (see `ride_stops` table). */
data class RideStopDto(
    @SerializedName("id") val id: String,
    @SerializedName("ride_id") val rideId: String,
    @SerializedName("address") val address: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("order_index") val orderIndex: Int,
    @SerializedName("arrived_at") val arrivedAt: String? = null,
    @SerializedName("left_at") val leftAt: String? = null,
    @SerializedName("wait_minutes") val waitMinutes: Double? = null,
)

/**
 * Backend always returns stops wrapped under `stops`, both for `GET` and `POST`.
 * Matches `res.json({ status: 'success', stops })` in the Express controller.
 */
data class RideStopsResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("stops") val stops: List<RideStopDto> = emptyList(),
)

fun RideStopDto.toDomain() =
    RideStop(
        id = id,
        rideId = rideId,
        address = address,
        lat = lat,
        lng = lng,
        orderIndex = orderIndex,
        arrivedAt = arrivedAt,
        leftAt = leftAt,
        waitMinutes = waitMinutes,
    )
