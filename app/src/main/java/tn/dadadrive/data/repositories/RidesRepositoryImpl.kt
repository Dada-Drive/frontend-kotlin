package tn.dadadrive.data.repositories

import android.util.Log
import tn.dadadrive.core.pricing.FareConfidence
import tn.dadadrive.core.pricing.FareQuote
import tn.dadadrive.data.network.api.RidesApiService
import tn.dadadrive.data.network.model.AddRideStopsRequestDto
import tn.dadadrive.data.network.model.CancelRideBodyDto
import tn.dadadrive.data.network.model.RequestRideRequestDto
import tn.dadadrive.data.network.model.RideStopRequestItemDto
import tn.dadadrive.data.network.model.SubmitRideRatingRequestDto
import tn.dadadrive.data.network.model.extractDriverRides
import tn.dadadrive.data.network.model.extractRiderRides
import tn.dadadrive.data.network.model.toDomain
import tn.dadadrive.domain.models.RideStop
import tn.dadadrive.domain.protocols.RidesRepository
import retrofit2.HttpException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.max

class RidesRepositoryImpl @Inject constructor(
    private val api: RidesApiService
) : RidesRepository {

    override suspend fun getFareOrFallback(distanceKm: Double, estimatedMinutes: Int): FareQuote {
        return try {
            FareQuote(
                fareTnd = api.getFare(distanceKm, estimatedMinutes).fare,
                confidence = FareConfidence.CONFIRMED
            )
        } catch (e: HttpException) {
            Log.w(TAG, "getFare HTTP ${e.code()} — fallback local", e)
            FareQuote(
                fareTnd = fallbackFare(distanceKm, estimatedMinutes),
                confidence = FareConfidence.NETWORK_FALLBACK
            )
        } catch (e: Exception) {
            Log.w(TAG, "getFare failed — fallback local", e)
            FareQuote(
                fareTnd = fallbackFare(distanceKm, estimatedMinutes),
                confidence = FareConfidence.NETWORK_FALLBACK
            )
        }
    }

    override fun localFareEstimate(distanceKm: Double, estimatedMinutes: Int): Double =
        fallbackFare(distanceKm, estimatedMinutes)

    override suspend fun requestRide(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffLat: Double,
        dropoffLng: Double,
        dropoffAddress: String,
        distanceKm: Double,
        estimatedMinutes: Int,
        vehicleType: String?,
        scheduledAtIso: String?,
        pickupForOther: Boolean,
        passengerName: String?,
        passengerPhone: String?
    ): Result<tn.dadadrive.domain.models.ActiveRide> {
        return try {
            val ride = api.requestRide(
                RequestRideRequestDto(
                    pickupLat = pickupLat,
                    pickupLng = pickupLng,
                    pickupAddress = pickupAddress,
                    dropoffLat = dropoffLat,
                    dropoffLng = dropoffLng,
                    dropoffAddress = dropoffAddress,
                    distanceKm = distanceKm,
                    estimatedMinutes = estimatedMinutes,
                    vehicleType = vehicleType,
                    scheduledAt = scheduledAtIso,
                    pickupForOther = pickupForOther,
                    passengerName = passengerName,
                    passengerPhone = passengerPhone
                )
            ).ride.toDomain()
            Result.success(ride)
        } catch (e: HttpException) {
            Result.failure(IllegalStateException(extractApiMessage(e) ?: "HTTP ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRideOffers(rideId: String) = runCatching {
        api.getRideOffers(rideId).offers.map { it.toDomain() }
    }

    override suspend fun getMyRides() = runCatching {
        api.getMyRides().extractRiderRides().map { it.toDomain() }
    }

    override suspend fun getScheduledRides() = runCatching {
        api.getScheduledRides().extractDriverRides().map { it.toDomain() }
    }

    override suspend fun submitRideRating(rideId: String, score: Int, comment: String?) = runCatching {
        api.submitRideRating(
            rideId = rideId,
            body = SubmitRideRatingRequestDto(score = score, comment = comment)
        ).rating.toDomain()
    }

    override suspend fun getRideRating(rideId: String) = runCatching {
        api.getRideRating(rideId).rating.toDomain()
    }

    override suspend fun getDriverRatings(
        driverId: String,
        page: Int,
        limit: Int
    ) = runCatching {
        val response = api.getDriverRatings(driverId = driverId, page = page, limit = limit)
        val ratings = response.ratings.map { it.toDomain() }
        val stats = response.stats?.toDomain()
            ?: tn.dadadrive.domain.models.DriverRatingsStats(avgRating = 0.0, totalRatings = 0)
        ratings to stats
    }

    override suspend fun pickRideOffer(rideId: String, offerId: String) = runCatching {
        api.pickRideOffer(rideId, offerId).ride.toDomain()
    }

    override suspend fun cancelRideRequest(rideId: String, reason: String?) = runCatching {
        api.cancelRideRequest(rideId, CancelRideBodyDto(reason)).ride.toDomain()
    }

    override suspend fun addRideStops(
        rideId: String,
        stops: List<RideStop>
    ) = runCatching {
        val body = AddRideStopsRequestDto(
            stops = stops.map {
                RideStopRequestItemDto(address = it.address, lat = it.lat, lng = it.lng)
            }
        )
        api.addRideStops(rideId, body).stops.map { it.toDomain() }
    }

    override suspend fun getRideStops(rideId: String) = runCatching {
        api.getRideStops(rideId).stops.map { it.toDomain() }
    }

    override suspend fun getNearbyTaxis(
        lat: Double,
        lng: Double,
        radiusKm: Double
    ) = runCatching {
        api.getNearbyDrivers(lat = lat, lng = lng, radiusKm = radiusKm)
            .drivers.orEmpty()
            .map { it.toDomain() }
    }

    /**
     * Copie la formule backend (`fareConfig.js`) pour le mode dégradé uniquement —
     * pas de fichier de config client : la source de vérité reste le serveur.
     */
    private fun fallbackFare(_distanceKm: Double, estimatedMinutes: Int): Double {
        val raw = FALLBACK_BASE_FARE +
            estimatedMinutes * FALLBACK_PRICE_PER_MINUTE
        val clamped = max(raw, FALLBACK_MIN_FARE)
        return BigDecimal.valueOf(clamped).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    private companion object {
        private const val TAG = "RidesRepository"
        private const val FALLBACK_BASE_FARE = 1.5
        // Align with backend fareConfig.js (PRICE_PER_MINUTE = 0.5).
        private const val FALLBACK_PRICE_PER_MINUTE = 0.5
        private const val FALLBACK_MIN_FARE = 3.0
    }

    private fun extractApiMessage(e: HttpException): String? {
        val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull()?.trim().orEmpty()
        if (raw.isBlank()) return null
        return runCatching {
            val json = JSONObject(raw)
            when {
                json.has("message") -> json.getString("message")
                json.has("errors") -> {
                    val arr = json.getJSONArray("errors")
                    if (arr.length() > 0) {
                        arr.getJSONObject(0).optString("msg").takeIf { it.isNotBlank() }
                            ?: raw
                    } else {
                        raw
                    }
                }
                else -> raw
            }
        }.getOrElse { raw }
    }
}
