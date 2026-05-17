package tn.turbodrive.domain.protocols

import tn.turbodrive.core.pricing.FareQuote
import tn.turbodrive.domain.models.ActiveRide
import tn.turbodrive.domain.models.DriverRatingsStats
import tn.turbodrive.domain.models.NearbyTaxi
import tn.turbodrive.domain.models.PassengerRideOffer
import tn.turbodrive.domain.models.RideRating
import tn.turbodrive.domain.models.RideStop

/**
 * Tarif côté serveur avec repli local si l’appel échoue (réseau, 401, etc.).
 */
interface RidesRepository {
    suspend fun getFareOrFallback(
        distanceKm: Double,
        estimatedMinutes: Int,
    ): FareQuote

    /** Même formule que le repli réseau — pour affichage immédiat avant les appels serveur. */
    fun localFareEstimate(
        distanceKm: Double,
        estimatedMinutes: Int,
    ): Double

    suspend fun requestRide(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffLat: Double,
        dropoffLng: Double,
        dropoffAddress: String,
        distanceKm: Double,
        estimatedMinutes: Int,
        vehicleType: String? = null,
        scheduledAtIso: String? = null,
        pickupForOther: Boolean = false,
        passengerName: String? = null,
        passengerPhone: String? = null,
    ): Result<ActiveRide>

    suspend fun getRideOffers(rideId: String): Result<List<PassengerRideOffer>>

    suspend fun getMyRides(): Result<List<ActiveRide>>

    suspend fun getScheduledRides(): Result<List<ActiveRide>>

    suspend fun submitRideRating(
        rideId: String,
        score: Int,
        comment: String? = null,
    ): Result<RideRating>

    suspend fun getRideRating(rideId: String): Result<RideRating>

    suspend fun getDriverRatings(
        driverId: String,
        page: Int = 1,
        limit: Int = 20,
    ): Result<Pair<List<RideRating>, DriverRatingsStats>>

    suspend fun getNearbyTaxis(
        lat: Double,
        lng: Double,
        radiusKm: Double = 5.0,
    ): Result<List<NearbyTaxi>>

    suspend fun pickRideOffer(
        rideId: String,
        offerId: String,
    ): Result<ActiveRide>

    suspend fun cancelRideRequest(
        rideId: String,
        reason: String? = null,
    ): Result<ActiveRide>

    /**
     * Posts intermediate stops for an existing ride. Each stop needs an address + lat/lng.
     * Server-side allowed while the ride is in pending / offered / accepted state.
     */
    suspend fun addRideStops(
        rideId: String,
        stops: List<RideStop>,
    ): Result<List<RideStop>>

    suspend fun getRideStops(rideId: String): Result<List<RideStop>>
}
