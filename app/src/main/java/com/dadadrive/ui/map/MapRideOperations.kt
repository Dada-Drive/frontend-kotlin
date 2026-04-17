package com.dadadrive.ui.map

import android.content.Context
import com.dadadrive.R
import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.DriverRatingsStats
import com.dadadrive.domain.model.PassengerRideOffer
import com.dadadrive.domain.model.RideRating
import com.dadadrive.domain.model.RideStatus
import com.dadadrive.core.pricing.RiderFareEstimate
import com.dadadrive.domain.repository.RidesRepository
import com.here.sdk.core.GeoCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
) {
    private var rideOffersPollingJob: Job? = null

    fun stopRideOffersPolling() {
        rideOffersPollingJob?.cancel()
        rideOffersPollingJob = null
        isLoadingRideOffers.value = false
    }

    fun requestRide() {
        val pickup = passengerRouting.effectivePickupGeo() ?: run {
            rideRequestError.value = context.getString(R.string.map_error_pickup_location_missing)
            return
        }
        val destination = confirmedDestination.value ?: run {
            rideRequestError.value = context.getString(R.string.map_error_destination_missing)
            return
        }
        val pickupAddress = pickupOverrideLabel.value ?: currentAddress.value ?: run {
            rideRequestError.value = context.getString(R.string.map_error_pickup_address_missing)
            return
        }
        val destinationAddress = destinationLabel.value ?: run {
            rideRequestError.value = context.getString(R.string.map_error_destination_address_missing)
            return
        }
        val routeOption = passengerRouteOptions.value
            .getOrNull(selectedPassengerRouteIndex.value)
            ?: riderFareEstimate.value?.let {
                PassengerRouteOption(
                    distanceKm = it.distanceKm,
                    estimatedMinutes = it.estimatedMinutes,
                    fareTnd = it.fareTnd
                )
            }
            ?: run {
                rideRequestError.value = context.getString(R.string.map_error_route_not_ready)
                return
            }

        val pickupNow = ridePickupNow.value
        val forMe = rideForMe.value
        val minScheduleMs = System.currentTimeMillis() + 30 * 60 * 1000L
        val scheduledAtIso = if (pickupNow) {
            null
        } else {
            val ms = (rideScheduledAtEpochMs.value ?: (System.currentTimeMillis() + 45 * 60 * 1000L))
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
                pickupAddress = pickupAddress,
                dropoffLat = destination.latitude,
                dropoffLng = destination.longitude,
                dropoffAddress = destinationAddress,
                distanceKm = routeOption.distanceKm,
                estimatedMinutes = routeOption.estimatedMinutes,
                vehicleType = "economy",
                scheduledAtIso = scheduledAtIso,
                pickupForOther = !forMe,
                passengerName = passengerBookingName.value.trim().takeIf { it.isNotEmpty() },
                passengerPhone = if (!forMe) passengerBookingPhone.value.trim() else null
            ).onSuccess { ride ->
                lastRequestedRide.value = ride
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
                    incomingRideOffers.value = emptyList()
                    lastRequestedRide.value = null
                    pickingOfferId.value = null
                    matchedRideOffer.value = null
                    isRideMatched.value = false
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
                    scheduledRides.value = rides
                        .filter { it.status == RideStatus.Scheduled }
                        .sortedBy { it.scheduledAt ?: "" }
                }
                .onFailure {
                    scheduledRidesError.value = context.getString(R.string.map_error_load_scheduled_rides_failed)
                }
            isLoadingScheduledRides.value = false
        }
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

    fun submitRideRating(rideId: String, score: Int, comment: String? = null) {
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

    fun fetchDriverRatings(driverId: String, page: Int = 1, limit: Int = 20) {
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
        rideOffersPollingJob = scope.launch {
            while (isActive) {
                isLoadingRideOffers.value = true
                ridesRepository.getRideOffers(rideId)
                    .onSuccess { offers -> incomingRideOffers.value = offers }
                    .onFailure { err ->
                        rideRequestError.value = err.message
                            ?: context.getString(R.string.map_error_load_offers_failed)
                    }
                isLoadingRideOffers.value = false
                delay(2500L)
            }
        }
    }

    private fun formatRideScheduledAtUtc(epochMs: Long): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date(epochMs))
    }
}
