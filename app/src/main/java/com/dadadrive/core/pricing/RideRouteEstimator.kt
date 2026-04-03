package com.dadadrive.core.pricing

import com.here.sdk.core.GeoCoordinates
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Estimates road distance and trip minutes so [FareCalculator] matches what riders send to
 * `POST /rides` (see backend). Swift repo does not implement this client-side; HERE routing
 * could replace the heuristic later.
 */
object RideRouteEstimator {

    /** Great-circle distance tends to underestimate driving distance — light detour factor. */
    private const val ROAD_DISTANCE_FACTOR = 1.25

    /** Derive minutes for the fare formula (backend expects `estimated_minutes` ≥ 1). */
    private const val ASSUMED_URBAN_SPEED_KMH = 28.0

    fun haversineKm(a: GeoCoordinates, b: GeoCoordinates): Double =
        haversineKm(a.latitude, a.longitude, b.latitude, b.longitude)

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val r1 = Math.toRadians(lat1)
        val r2 = Math.toRadians(lat2)
        val h = sin(dLat / 2).pow(2.0) +
            cos(r1) * cos(r2) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(h), sqrt(1 - h))
        return earthKm * c
    }

    /**
     * @return Pair(distanceKm for API/fare, estimatedMinutes for API/fare)
     */
    fun estimateDistanceAndMinutes(straightLineKm: Double): Pair<Double, Int> {
        val distanceKm = max(0.1, straightLineKm * ROAD_DISTANCE_FACTOR)
        val minutes = max(1, (distanceKm / ASSUMED_URBAN_SPEED_KMH * 60.0).roundToInt())
        return distanceKm to minutes
    }

    fun estimateFareFromPickupToDrop(pickup: GeoCoordinates, dropoff: GeoCoordinates): RiderFareEstimate {
        val straight = haversineKm(pickup, dropoff)
        val (distanceKm, minutes) = estimateDistanceAndMinutes(straight)
        val fare = FareCalculator.calculateFare(distanceKm, minutes)
        return RiderFareEstimate(
            straightLineKm = straight,
            distanceKm = distanceKm,
            estimatedMinutes = minutes,
            fareTnd = fare
        )
    }
}

data class RiderFareEstimate(
    val straightLineKm: Double,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val fareTnd: Double
)
