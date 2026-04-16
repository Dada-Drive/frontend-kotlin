package com.dadadrive.domain.repository

import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.DriverRatingsStats
import com.dadadrive.domain.model.PassengerRideOffer
import com.dadadrive.domain.model.RideRating

/**
 * Tarif côté serveur avec repli local si l’appel échoue (réseau, 401, etc.).
 */
interface RidesRepository {
    suspend fun getFareOrFallback(distanceKm: Double, estimatedMinutes: Int): Double

    /** Même formule que le repli réseau — pour affichage immédiat avant les appels serveur. */
    fun localFareEstimate(distanceKm: Double, estimatedMinutes: Int): Double

    suspend fun requestRide(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffLat: Double,
        dropoffLng: Double,
        dropoffAddress: String,
        distanceKm: Double,
        estimatedMinutes: Int,
        vehicleType: String = "economy",
        scheduledAtIso: String? = null,
        pickupForOther: Boolean = false,
        passengerName: String? = null,
        passengerPhone: String? = null
    ): Result<ActiveRide>

    suspend fun getRideOffers(rideId: String): Result<List<PassengerRideOffer>>
    suspend fun getScheduledRides(): Result<List<ActiveRide>>
    suspend fun submitRideRating(rideId: String, score: Int, comment: String? = null): Result<RideRating>
    suspend fun getRideRating(rideId: String): Result<RideRating>
    suspend fun getDriverRatings(
        driverId: String,
        page: Int = 1,
        limit: Int = 20
    ): Result<Pair<List<RideRating>, DriverRatingsStats>>
    suspend fun pickRideOffer(rideId: String, offerId: String): Result<ActiveRide>
    suspend fun cancelRideRequest(rideId: String, reason: String? = null): Result<ActiveRide>
}
