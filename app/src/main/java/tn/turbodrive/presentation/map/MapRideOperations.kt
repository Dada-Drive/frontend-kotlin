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
import tn.turbodrive.domain.models.ErrorCategory
import tn.turbodrive.domain.models.PassengerRideOffer
import tn.turbodrive.domain.models.PresentableError
import tn.turbodrive.domain.models.RideRating
import tn.turbodrive.domain.models.RideStatus
import tn.turbodrive.domain.models.RideStop
import tn.turbodrive.domain.protocols.RidesRepository
import tn.turbodrive.presentation.common.ScreenState
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
    private val rideRequestState: MutableStateFlow<ScreenState<Unit>>,
    private val lastRequestedRide: MutableStateFlow<ActiveRide?>,
    private val rideOffersState: MutableStateFlow<ScreenState<List<PassengerRideOffer>>>,
    private val pickingOfferId: MutableStateFlow<String?>,
    private val matchedRideOffer: MutableStateFlow<PassengerRideOffer?>,
    private val isRideMatched: MutableStateFlow<Boolean>,
    private val scheduledRidesState: MutableStateFlow<ScreenState<List<ActiveRide>>>,
    private val rideRatingState: MutableStateFlow<ScreenState<RideRating>>,
    private val submitRatingState: MutableStateFlow<ScreenState<Unit>>,
    private val driverRatingsStats: MutableStateFlow<DriverRatingsStats>,
    /** Supplies any rider-entered intermediate stops, filtered to only on-route ones. */
    private val pendingIntermediateStopsProvider: () -> List<RideStop> = { emptyList() },
    private val onIntermediateStopsPosted: () -> Unit = {},
) {
    private var rideOffersPollingJob: Job? = null
    private var rideStatusPollingJob: Job? = null
    private val tag = "MapRideOperations"

    private fun errState(msg: String): ScreenState.Error = ScreenState.Error(PresentableError(msg, ErrorCategory.Server, false))

    fun stopRideOffersPolling() {
        rideOffersPollingJob?.cancel()
        rideOffersPollingJob = null
        if (rideOffersState.value is ScreenState.Loading) rideOffersState.value = ScreenState.Idle
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
                rideRequestState.value = errState(context.getString(R.string.map_error_pickup_location_missing))
                return
            }
        val destination =
            confirmedDestination.value ?: run {
                rideRequestState.value = errState(context.getString(R.string.map_error_destination_missing))
                return
            }
        val pickupAddress =
            pickupOverrideLabel.value ?: currentAddress.value ?: run {
                rideRequestState.value = errState(context.getString(R.string.map_error_pickup_address_missing))
                return
            }
        val destinationAddress =
            destinationLabel.value ?: run {
                rideRequestState.value = errState(context.getString(R.string.map_error_destination_address_missing))
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
                    rideRequestState.value = errState(context.getString(R.string.map_error_route_not_ready))
                    return
                }
        val normalizedPickupAddress = pickupAddress.trim()
        val normalizedDestinationAddress = destinationAddress.trim()
        if (normalizedPickupAddress.isBlank()) {
            rideRequestState.value = errState(context.getString(R.string.map_error_pickup_address_missing))
            return
        }
        if (normalizedDestinationAddress.isBlank()) {
            rideRequestState.value = errState(context.getString(R.string.map_error_destination_address_missing))
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
                rideRequestState.value = errState(context.getString(R.string.map_error_passenger_phone_required))
                return
            }
        }

        scope.launch {
            rideRequestState.value = ScreenState.Loading
            lastRequestedRide.value = null
            rideOffersState.value = ScreenState.Idle
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
                rideRequestState.value = errState(err.message ?: context.getString(R.string.map_error_request_ride_failed))
            }

            if (rideRequestState.value is ScreenState.Loading) rideRequestState.value = ScreenState.Idle
        }
    }

    fun pickRideOffer(offerId: String) {
        val rideId = lastRequestedRide.value?.id ?: return
        val selectedOffer = (rideOffersState.value as? ScreenState.Loaded)?.value?.firstOrNull { it.id == offerId }
        scope.launch {
            rideRequestState.value = ScreenState.Loading
            pickingOfferId.value = offerId
            ridesRepository.pickRideOffer(rideId, offerId)
                .onSuccess {
                    stopRideOffersPolling()
                    rideOffersState.value = ScreenState.Idle
                    matchedRideOffer.value = selectedOffer
                    isRideMatched.value = true
                    startRideStatusPolling(rideId)
                    selectedOffer?.driverId?.let { fetchDriverRatings(it) }
                }
                .onFailure { err ->
                    rideRequestState.value = errState(err.message ?: context.getString(R.string.map_error_pick_driver_failed))
                }
            pickingOfferId.value = null
            if (rideRequestState.value is ScreenState.Loading) rideRequestState.value = ScreenState.Idle
        }
    }

    fun cancelRequestedRide() {
        val rideId = lastRequestedRide.value?.id ?: return
        scope.launch {
            rideRequestState.value = ScreenState.Loading
            ridesRepository.cancelRideRequest(rideId, "rider_cancelled_from_offer_picker")
                .onSuccess {
                    stopRideOffersPolling()
                    stopRideStatusPolling()
                    clearActiveRideState()
                    fetchScheduledRides()
                }
                .onFailure { err ->
                    rideRequestState.value = errState(err.message ?: context.getString(R.string.map_error_cancel_ride_failed))
                }
            if (rideRequestState.value is ScreenState.Loading) rideRequestState.value = ScreenState.Idle
        }
    }

    fun dismissIncomingOffer(offerId: String) {
        val current = (rideOffersState.value as? ScreenState.Loaded)?.value ?: emptyList()
        rideOffersState.value = ScreenState.Loaded(current.filterNot { it.id == offerId })
    }

    fun fetchScheduledRides() {
        scope.launch {
            scheduledRidesState.value = ScreenState.Loading
            ridesRepository.getScheduledRides()
                .onSuccess { rides ->
                    scheduledRidesState.value =
                        ScreenState.Loaded(
                            rides
                                .filter { it.status == RideStatus.Scheduled }
                                .sortedBy { it.scheduledAt ?: "" },
                        )
                }
                .onFailure {
                    scheduledRidesState.value = errState(context.getString(R.string.map_error_load_scheduled_rides_failed))
                }
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
                                rideRequestState.value = errState(context.getString(R.string.map_ride_cancelled_by_driver))
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
                                    rideRequestState.value = errState(context.getString(R.string.map_ride_cancelled_by_driver))
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
        rideOffersState.value = ScreenState.Idle
        lastRequestedRide.value = null
        pickingOfferId.value = null
        matchedRideOffer.value = null
        isRideMatched.value = false
        scope.launch { activeRideDraftCache.clear() }
    }

    fun fetchRideRating(rideId: String) {
        scope.launch {
            rideRatingState.value = ScreenState.Loading
            ridesRepository.getRideRating(rideId)
                .onSuccess { rating -> rideRatingState.value = ScreenState.Loaded(rating) }
                .onFailure { err ->
                    rideRatingState.value = errState(err.message ?: "")
                }
        }
    }

    fun submitRideRating(
        rideId: String,
        score: Int,
        comment: String? = null,
    ) {
        if (score !in 1..5) return
        scope.launch {
            submitRatingState.value = ScreenState.Loading
            ridesRepository.submitRideRating(rideId = rideId, score = score, comment = comment)
                .onSuccess { rating ->
                    rideRatingState.value = ScreenState.Loaded(rating)
                    submitRatingState.value = ScreenState.Idle
                }
                .onFailure { err ->
                    submitRatingState.value = errState(err.message ?: "")
                }
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
                    rideOffersState.value = ScreenState.Loading
                    ridesRepository.getRideOffers(rideId)
                        .onSuccess { offers ->
                            rideOffersState.value = ScreenState.Loaded(offers)
                            nextDelayMs = OFFERS_POLL_BASE_DELAY_MS
                            Log.d(tag, "offers polling success count=${offers.size} rideId=$rideId")
                        }
                        .onFailure { err ->
                            if (err is HttpException && err.code() in setOf(401, 403, 404)) {
                                // Ride no longer belongs to this session / role, or is no longer accessible.
                                // Continuing to poll here creates an infinite error loop that can freeze UI.
                                Log.w(tag, "stopping offers polling due to HTTP ${err.code()} rideId=$rideId")
                                rideOffersState.value = ScreenState.Idle
                                stopRideOffersPolling()
                                return@launch
                            }
                            rideOffersState.value = ScreenState.Idle
                            rideRequestState.value = errState(err.message ?: context.getString(R.string.map_error_load_offers_failed))
                            nextDelayMs =
                                when (err) {
                                    is UnknownHostException -> (nextDelayMs * 2).coerceAtMost(OFFERS_POLL_MAX_DELAY_MS)
                                    else -> (nextDelayMs + 2_500L).coerceAtMost(OFFERS_POLL_MAX_DELAY_MS)
                                }
                            Log.e(tag, "offers polling failed rideId=$rideId", err)
                        }
                    if (rideOffersState.value is ScreenState.Loading) rideOffersState.value = ScreenState.Idle
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
