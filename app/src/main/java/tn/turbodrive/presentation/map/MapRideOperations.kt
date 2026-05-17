package tn.turbodrive.presentation.map

import android.content.Context
import android.util.Log
import com.here.sdk.core.GeoCoordinates
import com.turbodrive.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import tn.turbodrive.core.pricing.RiderFareEstimate
import tn.turbodrive.data.local.ActiveRideDraftCache
import tn.turbodrive.domain.models.ActiveRide
import tn.turbodrive.domain.models.DriverRatingsStats
import tn.turbodrive.domain.models.PassengerRideOffer
import tn.turbodrive.domain.models.RideRating
import tn.turbodrive.domain.models.RideStatus
import tn.turbodrive.domain.models.RideStop
import tn.turbodrive.domain.protocols.RidesRepository
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Request ride, offers polling, scheduled rides, and ratings — API orchestration for the passenger map.
 */
internal class MapRideOperations(
    private val context: Context,
    private val scope: CoroutineScope,
    private val ridesRepository: RidesRepository,
    private val activeRideDraftCache: ActiveRideDraftCache,
    private val passengerRouting: MapPassengerRoutingController,
    private val confirmedDestination: MutableStateFlow<GeoCoordinates?>,
    private val pickupOverrideLabel: MutableStateFlow<String?>,
    private val currentAddress: MutableStateFlow<String?>,
    private val destinationLabel: MutableStateFlow<String?>,
    private val passengerRouteOptions: MutableStateFlow<List<PassengerRouteOption>>,
    private val selectedPassengerRouteIndex: MutableStateFlow<Int>,
    private val riderFareEstimate: MutableStateFlow<RiderFareEstimate?>,
    private val ridePickupNow: MutableStateFlow<Boolean>,
    private val rideForMe: MutableStateFlow<Boolean>,
    private val rideScheduledAtEpochMs: MutableStateFlow<Long?>,
    private val passengerBookingName: MutableStateFlow<String>,
    private val passengerBookingPhone: MutableStateFlow<String>,
    private val isRequestingRide: MutableStateFlow<Boolean>,
    private val rideRequestError: MutableStateFlow<String?>,
    private val lastRequestedRide: MutableStateFlow<ActiveRide?>,
    private val incomingRideOffers: MutableStateFlow<List<PassengerRideOffer>>,
    private val isLoadingRideOffers: MutableStateFlow<Boolean>,
    private val pickingOfferId: MutableStateFlow<String?>,
    private val matchedRideOffer: MutableStateFlow<PassengerRideOffer?>,
    private val isRideMatched: MutableStateFlow<Boolean>,
    private val scheduledRides: MutableStateFlow<List<ActiveRide>>,
    private val isLoadingScheduledRides: MutableStateFlow<Boolean>,
    private val scheduledRidesError: MutableStateFlow<String?>,
    private val rideRating: MutableStateFlow<RideRating?>,
    private val isLoadingRideRating: MutableStateFlow<Boolean>,
    private val rideRatingError: MutableStateFlow<String?>,
    private val isSubmittingRideRating: MutableStateFlow<Boolean>,
    private val submitRideRatingError: MutableStateFlow<String?>,
    private val driverRatingsStats: MutableStateFlow<DriverRatingsStats>,
    /** Supplies any rider-entered intermediate stops, filtered to only on-route ones. */
    private val pendingIntermediateStopsProvider: () -> List<RideStop> = { emptyList() },
    private val onIntermediateStopsPosted: () -> Unit = {},
) {
    private var rideOffersPollingJob: Job? = null
    private var rideStatusPollingJob: Job? = null
    private val tag = "MapRideOperations"

    fun stopRideOffersPolling() {
        rideOffersPollingJob?.cancel()
        rideOffersPollingJob = null
        isLoadingRideOffers.value = false
    }

    fun stopRideStatusPolling() {
        rideStatusPollingJob?.cancel()
        rideStatusPollingJob = null
    }

    /**
     * Fires a best-effort POST to `/rides/:id/stops` for every on-route stop the rider
     * entered before tapping Request. Failures are logged; the ride itself has already
     * been created, so we don't surface the error to the user.
     */
    private fun postIntermediateStopsIfAny(rideId: String) {
        val stops = pendingIntermediateStopsProvider()
        if (stops.isEmpty()) return
        scope.launch {
            ridesRepository.addRideStops(rideId, stops)
                .onSuccess { onIntermediateStopsPosted() }
                .onFailure { err ->
                    Log.w(tag, "addRideStops failed for ride=$rideId: ${err.message}", err)
                }
        }
    }

    fun requestRide() {
        val pickup =
            passengerRouting.effectivePickupGeo() ?: run {
                rideRequestError.value = context.getString(R.string.map_error_pickup_location_missing)
                return
            }
        val destination =
            confirmedDestination.value ?: run {
                rideRequestError.value = context.getString(R.string.map_error_destination_missing)
                return
            }
        val pickupAddress =
            pickupOverrideLabel.value ?: currentAddress.value ?: run {
                rideRequestError.value = context.getString(R.string.map_error_pickup_address_missing)
                return
            }
        val destinationAddress =
            destinationLabel.value ?: run {
                rideRequestError.value = context.getString(R.string.map_error_destination_address_missing)
                return
            }
        val routeOption =
            passengerRouteOptions.value
                .getOrNull(selectedPassengerRouteIndex.value)
                ?: riderFareEstimate.value?.let {
                    PassengerRouteOption(
                        distanceKm = it.distanceKm,
                        estimatedMinutes = it.estimatedMinutes,
                        fareTnd = it.fareTnd,
                    )
                }
                ?: run {
                    rideRequestError.value = context.getString(R.string.map_error_route_not_ready)
                    return
                }
        val normalizedPickupAddress = pickupAddress.trim()
        val normalizedDestinationAddress = destinationAddress.trim()
        if (normalizedPickupAddress.isBlank()) {
            rideRequestError.value = context.getString(R.string.map_error_pickup_address_missing)
            return
        }
        if (normalizedDestinationAddress.isBlank()) {
            rideRequestError.value = context.getString(R.string.map_error_destination_address_missing)
            return
        }
        val normalizedDistanceKm =
            routeOption.distanceKm
                .takeIf { it.isFinite() && !it.isNaN() }
                ?.coerceAtLeast(0.1)
                ?: 0.1
        val normalizedEstimatedMinutes = routeOption.estimatedMinutes.coerceAtLeast(1)

        val pickupNow = ridePickupNow.value
        val forMe = rideForMe.value
        val minScheduleMs = System.currentTimeMillis() + 30 * 60 * 1000L
        val scheduledAtIso =
            if (pickupNow) {
                null
            } else {
                val ms =
                    (rideScheduledAtEpochMs.value ?: (System.currentTimeMillis() + 45 * 60 * 1000L))
                        .coerceAtLeast(minScheduleMs)
                formatRideScheduledAtUtc(ms)
            }
        if (!forMe) {
            val phone = passengerBookingPhone.value.trim()
            if (phone.isEmpty()) {
                rideRequestError.value = context.getString(R.string.map_error_passenger_phone_required)
                return
            }
        }

        scope.launch {
            isRequestingRide.value = true
            rideRequestError.value = null
            lastRequestedRide.value = null
            incomingRideOffers.value = emptyList()
            pickingOfferId.value = null
            matchedRideOffer.value = null
            isRideMatched.value = false

            ridesRepository.requestRide(
                pickupLat = pickup.latitude,
                pickupLng = pickup.longitude,
                pickupAddress = normalizedPickupAddress,
                dropoffLat = destination.latitude,
                dropoffLng = destination.longitude,
                dropoffAddress = normalizedDestinationAddress,
                distanceKm = normalizedDistanceKm,
                estimatedMinutes = normalizedEstimatedMinutes,
                vehicleType = null,
                scheduledAtIso = scheduledAtIso,
                pickupForOther = !forMe,
                passengerName = passengerBookingName.value.trim().takeIf { it.isNotEmpty() },
                passengerPhone = if (!forMe) passengerBookingPhone.value.trim() else null,
            ).onSuccess { ride ->
                lastRequestedRide.value = ride
                postIntermediateStopsIfAny(ride.id)
                startRideStatusPolling(ride.id)
                if (ride.status == RideStatus.Completed) {
                    fetchRideRating(ride.id)
                }
                if (ride.status != RideStatus.Scheduled) {
                    startRideOffersPolling(ride.id)
                } else {
                    fetchScheduledRides()
                }
            }.onFailure { err ->
                rideRequestError.value = err.message
                    ?: context.getString(R.string.map_error_request_ride_failed)
            }

            isRequestingRide.value = false
        }
    }

    fun pickRideOffer(offerId: String) {
        val rideId = lastRequestedRide.value?.id ?: return
        val selectedOffer = incomingRideOffers.value.firstOrNull { it.id == offerId }
        scope.launch {
            isRequestingRide.value = true
            rideRequestError.value = null
            pickingOfferId.value = offerId
            ridesRepository.pickRideOffer(rideId, offerId)
                .onSuccess {
                    stopRideOffersPolling()
                    incomingRideOffers.value = emptyList()
                    matchedRideOffer.value = selectedOffer
                    isRideMatched.value = true
                    startRideStatusPolling(rideId)
                    selectedOffer?.driverId?.let { fetchDriverRatings(it) }
                }
                .onFailure { err ->
                    rideRequestError.value = err.message
                        ?: context.getString(R.string.map_error_pick_driver_failed)
                }
            pickingOfferId.value = null
            isRequestingRide.value = false
        }
    }

    fun cancelRequestedRide() {
        val rideId = lastRequestedRide.value?.id ?: return
        scope.launch {
            isRequestingRide.value = true
            rideRequestError.value = null
            ridesRepository.cancelRideRequest(rideId, "rider_cancelled_from_offer_picker")
                .onSuccess {
                    stopRideOffersPolling()
                    stopRideStatusPolling()
                    clearActiveRideState()
                    fetchScheduledRides()
                }
                .onFailure { err ->
                    rideRequestError.value = err.message
                        ?: context.getString(R.string.map_error_cancel_ride_failed)
                }
            isRequestingRide.value = false
        }
    }

    fun dismissIncomingOffer(offerId: String) {
        incomingRideOffers.value = incomingRideOffers.value.filterNot { it.id == offerId }
    }

    fun fetchScheduledRides() {
        scope.launch {
            isLoadingScheduledRides.value = true
            scheduledRidesError.value = null
            ridesRepository.getScheduledRides()
                .onSuccess { rides ->
                    scheduledRides.value =
                        rides
                            .filter { it.status == RideStatus.Scheduled }
                            .sortedBy { it.scheduledAt ?: "" }
                }
                .onFailure {
                    scheduledRidesError.value = context.getString(R.string.map_error_load_scheduled_rides_failed)
                }
            isLoadingScheduledRides.value = false
        }
    }

    private fun startRideStatusPolling(rideId: String) {
        stopRideStatusPolling()
        rideStatusPollingJob =
            scope.launch {
                while (isActive) {
                    ridesRepository.getMyRides()
                        .onSuccess { rides ->
                            val current = rides.firstOrNull { it.id == rideId }
                            if (current == null) {
                                stopRideOffersPolling()
                                stopRideStatusPolling()
                                clearActiveRideState()
                                rideRequestError.value = context.getString(R.string.map_ride_cancelled_by_driver)
                                return@launch
                            }
                            lastRequestedRide.value = current
                            when (current.status) {
                                RideStatus.Completed -> {
                                    stopRideOffersPolling()
                                    stopRideStatusPolling()
                                    fetchRideRating(rideId)
                                    return@launch
                                }
                                RideStatus.Cancelled -> {
                                    stopRideOffersPolling()
                                    stopRideStatusPolling()
                                    clearActiveRideState()
                                    rideRequestError.value = context.getString(R.string.map_ride_cancelled_by_driver)
                                    return@launch
                                }
                                else -> Unit
                            }
                        }
                    delay(RIDE_STATUS_POLL_MS)
                }
            }
    }

    private fun clearActiveRideState() {
        incomingRideOffers.value = emptyList()
        lastRequestedRide.value = null
        pickingOfferId.value = null
        matchedRideOffer.value = null
        isRideMatched.value = false
        scope.launch { activeRideDraftCache.clear() }
    }

    fun fetchRideRating(rideId: String) {
        scope.launch {
            isLoadingRideRating.value = true
            rideRatingError.value = null
            ridesRepository.getRideRating(rideId)
                .onSuccess { rating -> rideRating.value = rating }
                .onFailure { err ->
                    rideRating.value = null
                    rideRatingError.value = err.message
                }
            isLoadingRideRating.value = false
        }
    }

    fun submitRideRating(
        rideId: String,
        score: Int,
        comment: String? = null,
    ) {
        if (score !in 1..5) return
        scope.launch {
            isSubmittingRideRating.value = true
            submitRideRatingError.value = null
            ridesRepository.submitRideRating(rideId = rideId, score = score, comment = comment)
                .onSuccess { rating -> rideRating.value = rating }
                .onFailure { err ->
                    submitRideRatingError.value = err.message
                }
            isSubmittingRideRating.value = false
        }
    }

    fun fetchDriverRatings(
        driverId: String,
        page: Int = 1,
        limit: Int = 20,
    ) {
        scope.launch {
            ridesRepository.getDriverRatings(driverId = driverId, page = page, limit = limit)
                .onSuccess { (_, stats) ->
                    driverRatingsStats.value = stats
                }
                .onFailure { _ -> }
        }
    }

    private fun startRideOffersPolling(rideId: String) {
        stopRideOffersPolling()
        Log.d(tag, "startRideOffersPolling rideId=$rideId")
        rideOffersPollingJob =
            scope.launch {
                var nextDelayMs = OFFERS_POLL_BASE_DELAY_MS
                while (isActive) {
                    isLoadingRideOffers.value = true
                    ridesRepository.getRideOffers(rideId)
                        .onSuccess { offers ->
                            incomingRideOffers.value = offers
                            nextDelayMs = OFFERS_POLL_BASE_DELAY_MS
                            Log.d(tag, "offers polling success count=${offers.size} rideId=$rideId")
                        }
                        .onFailure { err ->
                            if (err is HttpException && err.code() in setOf(401, 403, 404)) {
                                // Ride no longer belongs to this session / role, or is no longer accessible.
                                // Continuing to poll here creates an infinite error loop that can freeze UI.
                                Log.w(tag, "stopping offers polling due to HTTP ${err.code()} rideId=$rideId")
                                incomingRideOffers.value = emptyList()
                                isLoadingRideOffers.value = false
                                stopRideOffersPolling()
                                return@launch
                            }
                            rideRequestError.value = err.message
                                ?: context.getString(R.string.map_error_load_offers_failed)
                            nextDelayMs =
                                when (err) {
                                    is UnknownHostException -> (nextDelayMs * 2).coerceAtMost(OFFERS_POLL_MAX_DELAY_MS)
                                    else -> (nextDelayMs + 2_500L).coerceAtMost(OFFERS_POLL_MAX_DELAY_MS)
                                }
                            Log.e(tag, "offers polling failed rideId=$rideId", err)
                        }
                    isLoadingRideOffers.value = false
                    delay(nextDelayMs)
                }
            }
    }

    private fun formatRideScheduledAtUtc(epochMs: Long): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date(epochMs))
    }

    private companion object {
        private const val OFFERS_POLL_BASE_DELAY_MS = 2_500L
        private const val OFFERS_POLL_MAX_DELAY_MS = 30_000L
        private const val RIDE_STATUS_POLL_MS = 3_500L
    }
}
