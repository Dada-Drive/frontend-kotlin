package tn.dadadrive.data.network.model

import tn.dadadrive.domain.models.NearbyTaxi
import com.google.gson.annotations.JsonAdapter

data class NearbyDriversResponseDto(
    val success: Boolean? = null,
    val drivers: List<NearbyDriverDto>? = null
)

@JsonAdapter(NearbyDriverDtoDeserializer::class)
data class NearbyDriverDto(
    val id: String,
    val fullName: String?,
    val lastLat: Double,
    val lastLng: Double,
    val lastHeading: Double?,
    val distanceKm: Double
)

fun NearbyDriverDto.toDomain() = NearbyTaxi(
    id = id,
    fullName = fullName,
    latitude = lastLat,
    longitude = lastLng,
    heading = lastHeading,
    distanceKm = distanceKm
)
