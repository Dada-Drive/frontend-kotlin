package com.dadadrive.data.remote.model

import com.dadadrive.domain.model.DriverRatingsStats
import com.dadadrive.domain.model.RideRating
import com.google.gson.annotations.SerializedName

data class SubmitRideRatingRequestDto(
    val score: Int,
    val comment: String? = null
)

data class RideRatingResponseDto(
    val success: Boolean? = null,
    val rating: RideRatingDto
)

data class RideRatingDto(
    val id: String,
    @SerializedName("ride_id") val rideId: String,
    @SerializedName("driver_id") val driverId: String?,
    val score: Int,
    val comment: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

fun RideRatingDto.toDomain() = RideRating(
    id = id,
    rideId = rideId,
    driverId = driverId,
    score = score,
    comment = comment,
    createdAt = createdAt
)

data class DriverRatingsResponseDto(
    val success: Boolean? = null,
    val ratings: List<RideRatingDto> = emptyList(),
    val stats: DriverRatingsStatsDto? = null
)

data class DriverRatingsStatsDto(
    @SerializedName("avg_rating") val avgRating: Double? = null,
    @SerializedName("total_ratings") val totalRatings: Int? = null
)

fun DriverRatingsStatsDto.toDomain() = DriverRatingsStats(
    avgRating = avgRating ?: 0.0,
    totalRatings = totalRatings ?: 0
)
