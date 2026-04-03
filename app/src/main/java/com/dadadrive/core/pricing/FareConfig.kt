package com.dadadrive.core.pricing

/**
 * Mirrors [backend/src/config/fareConfig.js] — keep in sync when fares change.
 */
object FareConfig {
    const val BASE_FARE: Double = 1.5
    const val PRICE_PER_KM: Double = 0.5
    const val PRICE_PER_MINUTE: Double = 0.1
    const val MIN_FARE: Double = 3.0
}
