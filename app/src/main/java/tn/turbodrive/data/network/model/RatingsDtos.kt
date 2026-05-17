package tn.turbodrive.data.network.model

import com.google.gson.annotations.SerializedName
import tn.turbodrive.domain.models.DriverRatingsStats
import tn.turbodrive.domain.models.RideRating

data class SubmitRideRatingRequestDto(
    val score: Int,
    val comment: String? = null,
)

data class RideRatingResponseDto(
    val rating: RideRatingDto,
)

data class RideRatingDto(
    val id: String,
    @SerializedName("ride_id") val rideId: String,
    @SerializedName("driver_id") val driverId: String?,
    val score: Int,
    val comment: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

fun RideRatingDto.toDomain() =
    RideRating(
        id = id,
        rideId = rideId,
        driverId = driverId,
        score = score,
        comment = comment,
        createdAt = createdAt,
    )

data class DriverRatingsResponseDto(
    val ratings: List<RideRatingDto> = emptyList(),
    val stats: DriverRatingsStatsDto? = null,
)

data class DriverRatingsStatsDto(
    @SerializedName("avg_rating") val avgRating: Double? = null,
    @SerializedName("total_ratings") val totalRatings: Int? = null,
)

fun DriverRatingsStatsDto.toDomain() =
    DriverRatingsStats(
        avgRating = avgRating ?: 0.0,
        totalRatings = totalRatings ?: 0,
    )
