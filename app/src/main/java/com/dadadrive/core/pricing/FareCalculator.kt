package com.dadadrive.core.pricing

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

/**
 * Same formula as [backend/src/services/rideService.js] `calculateFare`.
 */
object FareCalculator {

    fun calculateFare(distanceKm: Double, estimatedMinutes: Int): Double {
        val raw = FareConfig.BASE_FARE +
            distanceKm * FareConfig.PRICE_PER_KM +
            estimatedMinutes * FareConfig.PRICE_PER_MINUTE
        val clamped = max(raw, FareConfig.MIN_FARE)
        return BigDecimal.valueOf(clamped).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}
