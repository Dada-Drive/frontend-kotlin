package com.dadadrive.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.core.pricing.RiderFareEstimate
import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.DriverRatingsStats
import com.dadadrive.domain.model.PassengerRideOffer
import com.dadadrive.domain.model.RideRating
import com.dadadrive.domain.repository.RidesRepository
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.routing.RoutingEngine
import com.here.sdk.search.SearchEngine
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * Matches Swift LocationManager + SearchViewModel location logic.
 *
 * Domain logic is split into [MapPassengerRoutingController], [MapGeocodingHelper],
 * [MapRideOperations], [MapDriverPreviewRouting], and [MapLocationController].
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ridesRepository: RidesRepository
) : ViewModel() {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // ── GPS Position ────────────────────────────────────────────────────────

    private val _currentLocation = MutableStateFlow<GeoCoordinates?>(null)
    val currentLocation: StateFlow<GeoCoordinates?> = _currentLocation.asStateFlow()

    private val _locationAccuracy = MutableStateFlow<Float?>(null)
    val locationAccuracy: StateFlow<Float?> = _locationAccuracy.asStateFlow()

    private val _locationHeadingDegrees = MutableStateFlow<Float?>(null)
    val locationHeadingDegrees: StateFlow<Float?> = _locationHeadingDegrees.asStateFlow()

    private val _currentAddress = MutableStateFlow<String?>(null)
    val currentAddress: StateFlow<String?> = _currentAddress.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // ── Destination (single pick on map) ─────────────────────────────────────

    private val _pickTargetGeo = MutableStateFlow<GeoCoordinates?>(null)
    val pickTargetGeo: StateFlow<GeoCoordinates?> = _pickTargetGeo.asStateFlow()

    private val _pickTargetAddress = MutableStateFlow<String?>(null)
    val pickTargetAddress: StateFlow<String?> = _pickTargetAddress.asStateFlow()

    private val _confirmedDestination = MutableStateFlow<GeoCoordinates?>(null)
    val confirmedDestination: StateFlow<GeoCoordinates?> = _confirmedDestination.asStateFlow()

    private val _destinationLabel = MutableStateFlow<String?>(null)
    val destinationLabel: StateFlow<String?> = _destinationLabel.asStateFlow()

    private val _pickupOverrideGeo = MutableStateFlow<GeoCoordinates?>(null)
    val pickupOverrideGeo: StateFlow<GeoCoordinates?> = _pickupOverrideGeo.asStateFlow()

    private val _pickupOverrideLabel = MutableStateFlow<String?>(null)
    val pickupOverrideLabel: StateFlow<String?> = _pickupOverrideLabel.asStateFlow()

    private val _addressSearchResults = MutableStateFlow<List<AddressSearchHit>>(emptyList())
    val addressSearchResults: StateFlow<List<AddressSearchHit>> = _addressSearchResults.asStateFlow()

    private val _addressSearchLoading = MutableStateFlow(false)
    val addressSearchLoading: StateFlow<Boolean> = _addressSearchLoading.asStateFlow()

    private val _pickupSearchResults = MutableStateFlow<List<AddressSearchHit>>(emptyList())
    val pickupSearchResults: StateFlow<List<AddressSearchHit>> = _pickupSearchResults.asStateFlow()

    private val _pickupSearchLoading = MutableStateFlow(false)
    val pickupSearchLoading: StateFlow<Boolean> = _pickupSearchLoading.asStateFlow()

    private val _riderFareEstimate = MutableStateFlow<RiderFareEstimate?>(null)
    val riderFareEstimate: StateFlow<RiderFareEstimate?> = _riderFareEstimate.asStateFlow()

    private val _passengerRouteGeometries = MutableStateFlow<List<GeoPolyline>>(emptyList())
    val passengerRouteGeometries: StateFlow<List<GeoPolyline>> = _passengerRouteGeometries.asStateFlow()

    private val _passengerTrafficSpans = MutableStateFlow<List<PassengerTrafficSpan>>(emptyList())
    val passengerTrafficSpans: StateFlow<List<PassengerTrafficSpan>> = _passengerTrafficSpans.asStateFlow()

    private val _passengerRouteOptions = MutableStateFlow<List<PassengerRouteOption>>(emptyList())
    val passengerRouteOptions: StateFlow<List<PassengerRouteOption>> = _passengerRouteOptions.asStateFlow()

    private val _selectedPassengerRouteIndex = MutableStateFlow(0)
    val selectedPassengerRouteIndex: StateFlow<Int> = _selectedPassengerRouteIndex.asStateFlow()

    private val _driverPreviewRouteGeometries = MutableStateFlow<List<GeoPolyline>>(emptyList())
    val driverPreviewRouteGeometries: StateFlow<List<GeoPolyline>> = _driverPreviewRouteGeometries.asStateFlow()

    private val _isRequestingRide = MutableStateFlow(false)
    val isRequestingRide: StateFlow<Boolean> = _isRequestingRide.asStateFlow()

    private val _rideRequestError = MutableStateFlow<String?>(null)
    val rideRequestError: StateFlow<String?> = _rideRequestError.asStateFlow()

    private val _lastRequestedRide = MutableStateFlow<ActiveRide?>(null)
    val lastRequestedRide: StateFlow<ActiveRide?> = _lastRequestedRide.asStateFlow()

    private val _ridePickupNow = MutableStateFlow(true)
    val ridePickupNow: StateFlow<Boolean> = _ridePickupNow.asStateFlow()

    private val _rideForMe = MutableStateFlow(true)
    val rideForMe: StateFlow<Boolean> = _rideForMe.asStateFlow()

    private val _rideScheduledAtEpochMs = MutableStateFlow<Long?>(null)
    val rideScheduledAtEpochMs: StateFlow<Long?> = _rideScheduledAtEpochMs.asStateFlow()

    private val _passengerBookingName = MutableStateFlow("")
    val passengerBookingName: StateFlow<String> = _passengerBookingName.asStateFlow()

    private val _passengerBookingPhone = MutableStateFlow("")
    val passengerBookingPhone: StateFlow<String> = _passengerBookingPhone.asStateFlow()

    private val _incomingRideOffers = MutableStateFlow<List<PassengerRideOffer>>(emptyList())
    val incomingRideOffers: StateFlow<List<PassengerRideOffer>> = _incomingRideOffers.asStateFlow()

    private val _isLoadingRideOffers = MutableStateFlow(false)
    val isLoadingRideOffers: StateFlow<Boolean> = _isLoadingRideOffers.asStateFlow()

    private val _pickingOfferId = MutableStateFlow<String?>(null)
    val pickingOfferId: StateFlow<String?> = _pickingOfferId.asStateFlow()

    private val _matchedRideOffer = MutableStateFlow<PassengerRideOffer?>(null)
    val matchedRideOffer: StateFlow<PassengerRideOffer?> = _matchedRideOffer.asStateFlow()

    private val _isRideMatched = MutableStateFlow(false)
    val isRideMatched: StateFlow<Boolean> = _isRideMatched.asStateFlow()

    private val _scheduledRides = MutableStateFlow<List<ActiveRide>>(emptyList())
    val scheduledRides: StateFlow<List<ActiveRide>> = _scheduledRides.asStateFlow()

    private val _isLoadingScheduledRides = MutableStateFlow(false)
    val isLoadingScheduledRides: StateFlow<Boolean> = _isLoadingScheduledRides.asStateFlow()

    private val _scheduledRidesError = MutableStateFlow<String?>(null)
    val scheduledRidesError: StateFlow<String?> = _scheduledRidesError.asStateFlow()

    private val _rideRating = MutableStateFlow<RideRating?>(null)
    val rideRating: StateFlow<RideRating?> = _rideRating.asStateFlow()

    private val _isLoadingRideRating = MutableStateFlow(false)
    val isLoadingRideRating: StateFlow<Boolean> = _isLoadingRideRating.asStateFlow()

    private val _rideRatingError = MutableStateFlow<String?>(null)
    val rideRatingError: StateFlow<String?> = _rideRatingError.asStateFlow()

    private val _isSubmittingRideRating = MutableStateFlow(false)
    val isSubmittingRideRating: StateFlow<Boolean> = _isSubmittingRideRating.asStateFlow()

    private val _submitRideRatingError = MutableStateFlow<String?>(null)
    val submitRideRatingError: StateFlow<String?> = _submitRideRatingError.asStateFlow()

    private val _driverRatingsStats = MutableStateFlow(DriverRatingsStats(avgRating = 0.0, totalRatings = 0))
    val driverRatingsStats: StateFlow<DriverRatingsStats> = _driverRatingsStats.asStateFlow()

    private val routingEngine: RoutingEngine = RoutingEngine()

    private val searchEngine: SearchEngine? = try {
        SearchEngine()
    } catch (_: InstantiationErrorException) {
        null
    }

    private val passengerRouting = MapPassengerRoutingController(
        scope = viewModelScope,
        routingEngine = routingEngine,
        ridesRepository = ridesRepository,
        pickupOverride = _pickupOverrideGeo,
        currentLocation = _currentLocation,
        confirmedDestination = _confirmedDestination,
        riderFareEstimate = _riderFareEstimate,
        passengerRouteGeometries = _passengerRouteGeometries,
        passengerTrafficSpans = _passengerTrafficSpans,
        passengerRouteOptions = _passengerRouteOptions,
        selectedPassengerRouteIndex = _selectedPassengerRouteIndex,
    )

    private val geocodingHelper = MapGeocodingHelper(
        context = context,
        searchEngine = searchEngine,
        getBiasAnchor = { _currentLocation.value }
    )

    private val rideOps = MapRideOperations(
        context = context,
        scope = viewModelScope,
        ridesRepository = ridesRepository,
        passengerRouting = passengerRouting,
        confirmedDestination = _confirmedDestination,
        pickupOverrideLabel = _pickupOverrideLabel,
        currentAddress = _currentAddress,
        destinationLabel = _destinationLabel,
        passengerRouteOptions = _passengerRouteOptions,
        selectedPassengerRouteIndex = _selectedPassengerRouteIndex,
        riderFareEstimate = _riderFareEstimate,
        ridePickupNow = _ridePickupNow,
        rideForMe = _rideForMe,
        rideScheduledAtEpochMs = _rideScheduledAtEpochMs,
        passengerBookingName = _passengerBookingName,
        passengerBookingPhone = _passengerBookingPhone,
        isRequestingRide = _isRequestingRide,
        rideRequestError = _rideRequestError,
        lastRequestedRide = _lastRequestedRide,
        incomingRideOffers = _incomingRideOffers,
        isLoadingRideOffers = _isLoadingRideOffers,
        pickingOfferId = _pickingOfferId,
        matchedRideOffer = _matchedRideOffer,
        isRideMatched = _isRideMatched,
        scheduledRides = _scheduledRides,
        isLoadingScheduledRides = _isLoadingScheduledRides,
        scheduledRidesError = _scheduledRidesError,
        rideRating = _rideRating,
        isLoadingRideRating = _isLoadingRideRating,
        rideRatingError = _rideRatingError,
        isSubmittingRideRating = _isSubmittingRideRating,
        submitRideRatingError = _submitRideRatingError,
        driverRatingsStats = _driverRatingsStats,
    )

    private val driverPreviewRouting = MapDriverPreviewRouting(
        scope = viewModelScope,
        routingEngine = routingEngine,
    )

    private val locationController = MapLocationController(
        context = context,
        fusedLocationClient = fusedLocationClient,
        scope = viewModelScope,
        currentLocation = _currentLocation,
        locationAccuracy = _locationAccuracy,
        locationHeadingDegrees = _locationHeadingDegrees,
        currentAddress = _currentAddress,
        isTracking = _isTracking,
        onAfterFusedLocationFix = {
            passengerRouting.updateRiderFareEstimateIfPossible(fromGps = true)
        }
    )

    private var pickGeocodeJob: Job? = null
    private var forwardSearchJob: Job? = null
    private var pickupForwardSearchJob: Job? = null

    fun resetPickerDraft() {
        pickGeocodeJob?.cancel()
        _pickTargetGeo.value = null
        _pickTargetAddress.value = null
    }

    fun updatePickTarget(geo: GeoCoordinates) {
        val prev = _pickTargetGeo.value
        if (prev != null) {
            val dLat = kotlin.math.abs(prev.latitude - geo.latitude)
            val dLng = kotlin.math.abs(prev.longitude - geo.longitude)
            if (dLat < 1e-7 && dLng < 1e-7) return
        }
        _pickTargetGeo.value = geo
        pickGeocodeJob?.cancel()
        pickGeocodeJob = viewModelScope.launch {
            delay(320)
            val line = geocodingHelper.reverseGeocodeLine(geo.latitude, geo.longitude)
            _pickTargetAddress.value = line
        }
    }

    fun confirmDestination() {
        val geo = _pickTargetGeo.value ?: return
        _confirmedDestination.value = geo
        _destinationLabel.value = _pickTargetAddress.value
            ?: String.format(Locale.US, "%.5f, %.5f", geo.latitude, geo.longitude)
        passengerRouting.updateRiderFareEstimateIfPossible(fromGps = false)
    }

    fun confirmPickupOverrideFromPicker() {
        val geo = _pickTargetGeo.value ?: return
        _pickupOverrideGeo.value = geo
        _pickupOverrideLabel.value = _pickTargetAddress.value
            ?: String.format(Locale.US, "%.5f, %.5f", geo.latitude, geo.longitude)
        passengerRouting.updateRiderFareEstimateIfPossible(fromGps = false)
    }

    fun clearConfirmedDestination() {
        rideOps.stopRideOffersPolling()
        _confirmedDestination.value = null
        passengerRouting.clearPassengerRouteState()
        _incomingRideOffers.value = emptyList()
        _lastRequestedRide.value = null
        _pickingOfferId.value = null
        _matchedRideOffer.value = null
        _isRideMatched.value = false
    }

    fun setRidePickupNow(now: Boolean) {
        _ridePickupNow.value = now
        if (now) {
            _rideScheduledAtEpochMs.value = null
        } else if (_rideScheduledAtEpochMs.value == null) {
            _rideScheduledAtEpochMs.value = System.currentTimeMillis() + 45 * 60 * 1000L
        }
    }

    fun setRideForMe(forMe: Boolean) {
        _rideForMe.value = forMe
        if (forMe) {
            _passengerBookingName.value = ""
            _passengerBookingPhone.value = ""
        }
    }

    fun setRideScheduledAtEpochMs(epochMs: Long?) {
        _rideScheduledAtEpochMs.value = epochMs
    }

    fun setPassengerBookingName(value: String) {
        _passengerBookingName.value = value
    }

    fun setPassengerBookingPhone(value: String) {
        _passengerBookingPhone.value = value
    }

    fun updateDestinationLabelInput(text: String) {
        _destinationLabel.value = text.takeIf { it.isNotBlank() }
    }

    fun scheduleAddressSearch(query: String) {
        forwardSearchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length < 3) {
            _addressSearchResults.value = emptyList()
            _addressSearchLoading.value = false
            return
        }
        forwardSearchJob = viewModelScope.launch {
            _addressSearchLoading.value = true
            delay(MapViewModelConstants.ADDRESS_SEARCH_DEBOUNCE_MS)
            if (!isActive) return@launch
            val list = geocodingHelper.forwardGeocode(trimmed)
            if (!isActive) return@launch
            _addressSearchResults.value = list
            _addressSearchLoading.value = false
        }
    }

    fun clearAddressSearchResults() {
        forwardSearchJob?.cancel()
        _addressSearchResults.value = emptyList()
        _addressSearchLoading.value = false
    }

    fun applySearchDestination(hit: AddressSearchHit) {
        clearAddressSearchResults()
        _confirmedDestination.value = hit.coordinates
        _destinationLabel.value = hit.label
        passengerRouting.updateRiderFareEstimateIfPossible(fromGps = false)
    }

    fun selectPassengerRoute(index: Int) {
        passengerRouting.selectPassengerRoute(index)
    }

    fun applyPickupOverride(hit: AddressSearchHit) {
        clearPickupSearchResults()
        _pickupOverrideGeo.value = hit.coordinates
        _pickupOverrideLabel.value = hit.label
        passengerRouting.updateRiderFareEstimateIfPossible(fromGps = false)
    }

    fun clearPickupOverride() {
        clearPickupSearchResults()
        _pickupOverrideGeo.value = null
        _pickupOverrideLabel.value = null
        passengerRouting.updateRiderFareEstimateIfPossible(fromGps = false)
    }

    fun schedulePickupSearch(query: String) {
        pickupForwardSearchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length < 3) {
            _pickupSearchResults.value = emptyList()
            _pickupSearchLoading.value = false
            return
        }
        pickupForwardSearchJob = viewModelScope.launch {
            _pickupSearchLoading.value = true
            delay(MapViewModelConstants.ADDRESS_SEARCH_DEBOUNCE_MS)
            if (!isActive) return@launch
            val list = geocodingHelper.forwardGeocode(trimmed)
            if (!isActive) return@launch
            _pickupSearchResults.value = list
            _pickupSearchLoading.value = false
        }
    }

    fun clearPickupSearchResults() {
        pickupForwardSearchJob?.cancel()
        _pickupSearchResults.value = emptyList()
        _pickupSearchLoading.value = false
    }

    fun requestRide() = rideOps.requestRide()

    fun pickRideOffer(offerId: String) = rideOps.pickRideOffer(offerId)

    fun cancelRequestedRide() = rideOps.cancelRequestedRide()

    fun dismissIncomingOffer(offerId: String) = rideOps.dismissIncomingOffer(offerId)

    fun fetchScheduledRides() = rideOps.fetchScheduledRides()

    fun fetchRideRating(rideId: String) = rideOps.fetchRideRating(rideId)

    fun submitRideRating(rideId: String, score: Int, comment: String? = null) =
        rideOps.submitRideRating(rideId, score, comment)

    fun fetchDriverRatings(driverId: String, page: Int = 1, limit: Int = 20) =
        rideOps.fetchDriverRatings(driverId, page, limit)

    fun updateDriverPreviewRoutes(driverLocation: GeoCoordinates?, activeRide: ActiveRide?) {
        driverPreviewRouting.updateDriverPreviewRoutes(
            driverLocation = driverLocation,
            activeRide = activeRide,
            driverPreviewRouteGeometries = _driverPreviewRouteGeometries
        )
    }

    fun startLocationUpdates() = locationController.startLocationUpdates()

    fun stopLocationUpdates() = locationController.stopLocationUpdates()

    fun fetchLastLocation() = locationController.fetchLastLocation()

    override fun onCleared() {
        super.onCleared()
        rideOps.stopRideOffersPolling()
        locationController.stopLocationUpdates()
        passengerRouting.onViewModelCleared()
        routingEngine.dispose()
    }
}
