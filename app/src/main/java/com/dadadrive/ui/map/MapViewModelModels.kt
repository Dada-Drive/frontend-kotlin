package com.dadadrive.ui.map

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline

data class AddressSearchHit(
    val label: String,
    val coordinates: GeoCoordinates
)

data class PassengerTrafficSpan(
    val geometry: GeoPolyline,
    val jamFactor: Double
)

data class PassengerRouteOption(
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val fareTnd: Double
)
