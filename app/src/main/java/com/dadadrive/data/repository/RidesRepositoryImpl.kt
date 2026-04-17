package com.dadadrive.data.repository

import android.util.Log
import com.dadadrive.data.remote.api.RidesApiService
import com.dadadrive.data.remote.model.CancelRideBodyDto
import com.dadadrive.data.remote.model.RequestRideRequestDto
import com.dadadrive.data.remote.model.SubmitRideRatingRequestDto
import com.dadadrive.data.remote.model.toDomain
import com.dadadrive.domain.repository.RidesRepository
import retrofit2.HttpException
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.max

class RidesRepositoryImpl @Inject constructor(
    private val api: RidesApiService
) : RidesRepository {

    override suspend fun getFareOrFallback(distanceKm: Double, estimatedMinutes: Int): Double {
        return try {
            api.getFare(distanceKm, estimatedMinutes).fare
        } catch (e: HttpException) {
            Log.w(TAG, "getFare HTTP ${e.code()} — fallback local", e)
            fallbackFare(distanceKm, estimatedMinutes)
        } catch (e: Exception) {
            Log.w(TAG, "getFare failed — fallback local", e)
            fallbackFare(distanceKm, estimatedMinutes)
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
        vehicleType: String,
        scheduledAtIso: String?,
        pickupForOther: Boolean,
        passengerName: String?,
        passengerPhone: String?
    ) = runCatching {
        api.requestRide(
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
    }

    override suspend fun getRideOffers(rideId: String) = runCatching {
        api.getRideOffers(rideId).offers.map { it.toDomain() }
    }

    override suspend fun getScheduledRides() = runCatching {
        api.getScheduledRides().rides.map { it.toDomain() }
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
            ?: com.dadadrive.domain.model.DriverRatingsStats(avgRating = 0.0, totalRatings = 0)
        ratings to stats
    }

    override suspend fun pickRideOffer(rideId: String, offerId: String) = runCatching {
        api.pickRideOffer(rideId, offerId).ride.toDomain()
    }

    override suspend fun cancelRideRequest(rideId: String, reason: String?) = runCatching {
        api.cancelRideRequest(rideId, CancelRideBodyDto(reason)).ride.toDomain()
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
        private const val FALLBACK_PRICE_PER_MINUTE = 0.1
        private const val FALLBACK_MIN_FARE = 3.0
    }
}
