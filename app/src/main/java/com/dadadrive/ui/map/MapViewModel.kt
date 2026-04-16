package com.dadadrive.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.util.Log
import com.dadadrive.R
import com.dadadrive.core.pricing.RideRouteEstimator
import com.dadadrive.core.pricing.RiderFareEstimate
import com.dadadrive.domain.model.PassengerRideOffer
import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.DriverRatingsStats
import com.dadadrive.domain.model.RideRating
import com.dadadrive.domain.model.RideStatus
import com.dadadrive.domain.repository.RidesRepository
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.core.LanguageCode
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.routing.CalculateRouteCallback
import com.here.sdk.routing.CarOptions
import com.here.sdk.routing.Route
import com.here.sdk.routing.RoutingEngine
import com.here.sdk.routing.RoutingError
import com.here.sdk.routing.Waypoint
import com.here.sdk.search.ResponseDetails
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchError
import com.here.sdk.search.SearchOptions
import com.here.sdk.search.SuggestCallbackExtended
import com.here.sdk.search.Suggestion
import com.here.sdk.search.TextQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class AddressSearchHit(
    val label: String,
    val coordinates: GeoCoordinates
)

data class PassengerTrafficSpan(
    val geometry: GeoPolyline,
    val jamFactor: Double
)

data class PassengerRouteOption(
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val fareTnd: Double
)

/**
 * Matches Swift LocationManager + SearchViewModel location logic.
 *
 * Swift reference:
 *   LocationManager:  desiredAccuracy = kCLLocationAccuracyNearestTenMeters, distanceFilter = 8
 *   Location updates: continuous, with geocoding on each update
 *   Destination pick: center-pin with debounced reverse geocode (350ms in Swift)
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

    /** GPS accuracy in metres (uncertainty circle radius). */
    private val _locationAccuracy = MutableStateFlow<Float?>(null)
    val locationAccuracy: StateFlow<Float?> = _locationAccuracy.asStateFlow()

    /**
     * Heading for rotating the user / vehicle marker: degrees clockwise from true north (0–360°).
     * Null until a reliable bearing is available.
     */
    private val _locationHeadingDegrees = MutableStateFlow<Float?>(null)
    val locationHeadingDegrees: StateFlow<Float?> = _locationHeadingDegrees.asStateFlow()

    /** Previous fused fix used with [Location.distanceBetween] / [Location.bearingTo]. */
    private var lastSampleLat: Double? = null
    private var lastSampleLng: Double? = null

    /** Textual address of current position (via Geocoder). */
    private val _currentAddress = MutableStateFlow<String?>(null)
    val currentAddress: StateFlow<String?> = _currentAddress.asStateFlow()

    /** true when real-time GPS updates are active. */
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // ── Destination (single pick on map) ─────────────────────────────────────

    /** Geo point under screen center (destination pick mode). */
    private val _pickTargetGeo = MutableStateFlow<GeoCoordinates?>(null)
    val pickTargetGeo: StateFlow<GeoCoordinates?> = _pickTargetGeo.asStateFlow()

    /** Address displayed above the center pin (reverse geocode). */
    private val _pickTargetAddress = MutableStateFlow<String?>(null)
    val pickTargetAddress: StateFlow<String?> = _pickTargetAddress.asStateFlow()

    /** Confirmed destination after "Confirm" tap. */
    private val _confirmedDestination = MutableStateFlow<GeoCoordinates?>(null)
    val confirmedDestination: StateFlow<GeoCoordinates?> = _confirmedDestination.asStateFlow()

    /** Text shown in the "To" field of the route sheet. */
    private val _destinationLabel = MutableStateFlow<String?>(null)
    val destinationLabel: StateFlow<String?> = _destinationLabel.asStateFlow()

    /** Optional pickup point chosen in the route sheet (otherwise use GPS [currentLocation]). */
    private val _pickupOverrideGeo = MutableStateFlow<GeoCoordinates?>(null)
    val pickupOverrideGeo: StateFlow<GeoCoordinates?> = _pickupOverrideGeo.asStateFlow()

    private val _pickupOverrideLabel = MutableStateFlow<String?>(null)
    val pickupOverrideLabel: StateFlow<String?> = _pickupOverrideLabel.asStateFlow()

    /** Forward-geocode suggestions (HERE autosuggest when available, else Android Geocoder). */
    private val _addressSearchResults = MutableStateFlow<List<AddressSearchHit>>(emptyList())
    val addressSearchResults: StateFlow<List<AddressSearchHit>> = _addressSearchResults.asStateFlow()

    private val _addressSearchLoading = MutableStateFlow(false)
    val addressSearchLoading: StateFlow<Boolean> = _addressSearchLoading.asStateFlow()

    private val _pickupSearchResults = MutableStateFlow<List<AddressSearchHit>>(emptyList())
    val pickupSearchResults: StateFlow<List<AddressSearchHit>> = _pickupSearchResults.asStateFlow()

    private val _pickupSearchLoading = MutableStateFlow(false)
    val pickupSearchLoading: StateFlow<Boolean> = _pickupSearchLoading.asStateFlow()

    /** Pickup→drop : tarif via [com.dadadrive.domain.repository.RidesRepository] (`GET /rides/fare`) + repli local. */
    private val _riderFareEstimate = MutableStateFlow<RiderFareEstimate?>(null)
    val riderFareEstimate: StateFlow<RiderFareEstimate?> = _riderFareEstimate.asStateFlow()

    /**
     * HERE car routes (index 0 = fastest / main, further indices = alternatives).
     * Used to draw polylines on the passenger map; geometries are copied so native handles stay on the UI thread.
     */
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

    /** Maintenant vs à planifier (course programmée). */
    private val _ridePickupNow = MutableStateFlow(true)
    val ridePickupNow: StateFlow<Boolean> = _ridePickupNow.asStateFlow()

    /** Pour moi vs pour un autre passager au point de prise en charge. */
    private val _rideForMe = MutableStateFlow(true)
    val rideForMe: StateFlow<Boolean> = _rideForMe.asStateFlow()

    /** Heure de prise en charge (UTC côté API) — utilisé si [ridePickupNow] est false. */
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
    private var passengerRouteRequestId: Int = 0
    private var passengerRouteRefreshJob: Job? = null
    private var passengerTrafficByRoute: List<List<PassengerTrafficSpan>> = emptyList()

    /**
     * Last pickup/drop used when starting a HERE route request. GPS-only updates skip re-routing until
     * the user moves ~10 m from the pickup anchor (avoids invalidating in-flight [passengerRouteRequestId]).
     * The drop anchor lets us skip redundant debounced requests when endpoints are unchanged.
     */
    private var routeRefreshPickupAnchor: GeoCoordinates? = null
    private var routeRefreshDropAnchor: GeoCoordinates? = null

    private var trafficSpanMethodsDebugLogged: Boolean = false

    private var pickGeocodeJob: Job? = null
    private var forwardSearchJob: Job? = null
    private var pickupForwardSearchJob: Job? = null
    private var quickFareJob: Job? = null
    private var rideOffersPollingJob: Job? = null
    private var driverPreviewRouteRequestId: Int = 0

    fun resetPickerDraft() {
        pickGeocodeJob?.cancel()
        _pickTargetGeo.value = null
        _pickTargetAddress.value = null
    }

    /**
     * Update center-pin target. Debounced geocode at 320ms
     * (Swift uses 350ms for drag label).
     */
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
            val line = reverseGeocodeLine(geo.latitude, geo.longitude)
            _pickTargetAddress.value = line
        }
    }

    fun confirmDestination() {
        val geo = _pickTargetGeo.value ?: return
        _confirmedDestination.value = geo
        _destinationLabel.value = _pickTargetAddress.value
            ?: String.format(Locale.US, "%.5f, %.5f", geo.latitude, geo.longitude)
        updateRiderFareEstimateIfPossible(fromGps = false)
    }

    fun confirmPickupOverrideFromPicker() {
        val geo = _pickTargetGeo.value ?: return
        _pickupOverrideGeo.value = geo
        _pickupOverrideLabel.value = _pickTargetAddress.value
            ?: String.format(Locale.US, "%.5f, %.5f", geo.latitude, geo.longitude)
        updateRiderFareEstimateIfPossible(fromGps = false)
    }

    fun clearConfirmedDestination() {
        stopRideOffersPolling()
        _confirmedDestination.value = null
        quickFareJob?.cancel()
        _riderFareEstimate.value = null
        _passengerRouteGeometries.value = emptyList()
        _passengerTrafficSpans.value = emptyList()
        _passengerRouteOptions.value = emptyList()
        _selectedPassengerRouteIndex.value = 0
        passengerTrafficByRoute = emptyList()
        passengerRouteRequestId++
        passengerRouteRefreshJob?.cancel()
        routeRefreshPickupAnchor = null
        routeRefreshDropAnchor = null
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

    /**
     * Debounced forward geocode for typed queries. Uses HERE autosuggest (fast) with Geocoder fallback.
     */
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
            delay(ADDRESS_SEARCH_DEBOUNCE_MS)
            if (!isActive) return@launch
            val list = forwardGeocode(trimmed)
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

    /** Apply a chosen suggestion as the ride destination. */
    fun applySearchDestination(hit: AddressSearchHit) {
        clearAddressSearchResults()
        _confirmedDestination.value = hit.coordinates
        _destinationLabel.value = hit.label
        updateRiderFareEstimateIfPossible(fromGps = false)
    }

    private fun effectivePickupGeo(): GeoCoordinates? =
        _pickupOverrideGeo.value ?: _currentLocation.value

    /**
     * @param fromGps When true, only debounced HERE routing is scheduled if pickup moved enough from
     * [routeRefreshPickupAnchor]; avoids cancelling in-flight routes on every location tick.
     */
    private fun updateRiderFareEstimateIfPossible(fromGps: Boolean = false) {
        val pickup = effectivePickupGeo()
        val drop = _confirmedDestination.value
        if (pickup == null || drop == null) {
            quickFareJob?.cancel()
            _riderFareEstimate.value = null
            _passengerRouteGeometries.value = emptyList()
            _passengerTrafficSpans.value = emptyList()
            _passengerRouteOptions.value = emptyList()
            _selectedPassengerRouteIndex.value = 0
            passengerTrafficByRoute = emptyList()
            passengerRouteRequestId++
            passengerRouteRefreshJob?.cancel()
            routeRefreshPickupAnchor = null
            routeRefreshDropAnchor = null
            return
        }
        quickFareJob?.cancel()
        quickFareJob = viewModelScope.launch {
            val straight = RideRouteEstimator.haversineKm(pickup, drop)
            val (distanceKm, minutes) = RideRouteEstimator.estimateDistanceAndMinutes(straight)
            val fare = ridesRepository.getFareOrFallback(distanceKm, minutes)
            if (pickup != effectivePickupGeo() || drop != _confirmedDestination.value) return@launch
            // HERE routes (or optimistic polylines) own the fare UI; don't overwrite with straight-line API.
            if (_passengerRouteGeometries.value.isNotEmpty()) return@launch
            _riderFareEstimate.value = RiderFareEstimate(
                straightLineKm = straight,
                distanceKm = distanceKm,
                estimatedMinutes = minutes,
                fareTnd = fare
            )
        }
        if (fromGps) {
            maybeSchedulePassengerRouteRefreshAfterGpsMove(pickup)
        } else {
            schedulePassengerRouteRefresh(debounceMs = 0L)
        }
    }

    /**
     * HERE route refresh. Use [debounceMs] = 0 when the user just confirmed pickup/destination;
     * keep [PASSENGER_ROUTE_DEBOUNCE_MS] for GPS ticks to avoid spamming the routing service.
     */
    private fun schedulePassengerRouteRefresh(debounceMs: Long = PASSENGER_ROUTE_DEBOUNCE_MS) {
        passengerRouteRefreshJob?.cancel()
        passengerRouteRefreshJob = viewModelScope.launch {
            if (debounceMs > 0L) delay(debounceMs)
            val pickup = effectivePickupGeo() ?: return@launch
            val drop = _confirmedDestination.value ?: return@launch
            val pAnchor = routeRefreshPickupAnchor
            val dAnchor = routeRefreshDropAnchor
            if (pAnchor != null && dAnchor != null &&
                distanceMeters(pickup, pAnchor) <= PASSENGER_ROUTE_GPS_RESCHEDULE_METERS &&
                distanceMeters(drop, dAnchor) <= PASSENGER_ROUTE_GPS_RESCHEDULE_METERS
            ) {
                return@launch
            }
            requestPassengerRouting(pickup, drop)
        }
    }

    private fun maybeSchedulePassengerRouteRefreshAfterGpsMove(currentPickup: GeoCoordinates) {
        val anchor = routeRefreshPickupAnchor
        if (anchor == null) {
            schedulePassengerRouteRefresh()
            return
        }
        if (distanceMeters(anchor, currentPickup) > PASSENGER_ROUTE_GPS_RESCHEDULE_METERS) {
            schedulePassengerRouteRefresh()
        }
    }

    private fun distanceMeters(a: GeoCoordinates, b: GeoCoordinates): Float {
        val out = FloatArray(1)
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, out)
        return out[0]
    }

    private fun requestPassengerRouting(pickup: GeoCoordinates, drop: GeoCoordinates) {
        routeRefreshPickupAnchor = pickup
        routeRefreshDropAnchor = drop
        val requestId = ++passengerRouteRequestId
        val waypoints = listOf(Waypoint(pickup), Waypoint(drop))
        val options = CarOptions().also { car ->
            car.routeOptions.apply {
                // Up to two extra routes besides the best (traffic-aware when enabled on options / server).
                alternatives = 2
            }
        }
        routingEngine.calculateRoute(waypoints, options, object : CalculateRouteCallback {
            override fun onRouteCalculated(routingError: RoutingError?, routes: MutableList<Route>?) {
                if (requestId != passengerRouteRequestId) return
                if (routingError != null) {
                    Log.w(TAG_ROUTE, "calculateRoute: $routingError")
                    viewModelScope.launch(Dispatchers.Main) {
                        if (requestId == passengerRouteRequestId) {
                            routeRefreshPickupAnchor = null
                            routeRefreshDropAnchor = null
                            _passengerRouteGeometries.value = emptyList()
                            _passengerTrafficSpans.value = emptyList()
                            _passengerRouteOptions.value = emptyList()
                            _selectedPassengerRouteIndex.value = 0
                            passengerTrafficByRoute = emptyList()
                        }
                    }
                    return
                }
                val routesSnapshot = routes.orEmpty().toList()
                data class BuiltRoute(
                    val polyline: GeoPolyline,
                    val distanceKm: Double,
                    val minutes: Int,
                    val traffic: List<PassengerTrafficSpan>
                )
                val built = ArrayList<BuiltRoute>(routesSnapshot.size)
                for (route in routesSnapshot) {
                    val geometry = route.geometry ?: continue
                    val copied = copyGeometry(geometry) ?: continue
                    val distanceKm = route.lengthInMeters / 1000.0
                    val minutes = RideRouteEstimator.estimateDistanceAndMinutes(distanceKm).second
                    built.add(
                        BuiltRoute(
                            polyline = copied,
                            distanceKm = distanceKm,
                            minutes = minutes,
                            traffic = emptyList()
                        )
                    )
                }
                if (built.isEmpty()) {
                    viewModelScope.launch(Dispatchers.Main) {
                        if (requestId == passengerRouteRequestId) {
                            routeRefreshPickupAnchor = null
                            routeRefreshDropAnchor = null
                            _passengerRouteGeometries.value = emptyList()
                            _passengerTrafficSpans.value = emptyList()
                            _passengerRouteOptions.value = emptyList()
                            _selectedPassengerRouteIndex.value = 0
                            passengerTrafficByRoute = emptyList()
                        }
                    }
                    return
                }
                val defaultSelected = 0
                Log.d(
                    TAG_ROUTE,
                    "requestPassengerRouting: builtRoutes=${built.size}, show polylines ASAP; traffic + fares async"
                )
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    if (requestId != passengerRouteRequestId) return@launch
                    val polylines = built.map { it.polyline }
                    val optimisticOptions = built.map { b ->
                        PassengerRouteOption(
                            distanceKm = b.distanceKm,
                            estimatedMinutes = b.minutes,
                            fareTnd = ridesRepository.localFareEstimate(b.distanceKm, b.minutes)
                        )
                    }
                    _passengerRouteGeometries.value = polylines
                    _passengerTrafficSpans.value = emptyList()
                    _passengerRouteOptions.value = optimisticOptions
                    _selectedPassengerRouteIndex.value = defaultSelected
                    passengerTrafficByRoute = List(built.size) { emptyList() }
                    _riderFareEstimate.value = optimisticOptions.getOrNull(defaultSelected)?.let { option ->
                        RiderFareEstimate(
                            straightLineKm = option.distanceKm,
                            distanceKm = option.distanceKm,
                            estimatedMinutes = option.estimatedMinutes,
                            fareTnd = option.fareTnd
                        )
                    }
                }
                viewModelScope.launch(Dispatchers.Default) {
                    val trafficByRoute = routesSnapshot.map { extractTrafficSpans(it) }
                    withContext(Dispatchers.Main.immediate) {
                        if (requestId != passengerRouteRequestId) return@withContext
                        passengerTrafficByRoute = trafficByRoute
                        val idx = _selectedPassengerRouteIndex.value
                            .coerceIn(0, trafficByRoute.lastIndex.coerceAtLeast(0))
                        _passengerTrafficSpans.value = trafficByRoute.getOrNull(idx).orEmpty()
                    }
                }
                viewModelScope.launch(Dispatchers.IO) {
                    val fares = coroutineScope {
                        built.map { b ->
                            async { ridesRepository.getFareOrFallback(b.distanceKm, b.minutes) }
                        }.awaitAll()
                    }
                    withContext(Dispatchers.Main.immediate) {
                        if (requestId != passengerRouteRequestId) return@withContext
                        val routeOptions = built.mapIndexed { i, b ->
                            PassengerRouteOption(
                                distanceKm = b.distanceKm,
                                estimatedMinutes = b.minutes,
                                fareTnd = fares[i]
                            )
                        }
                        val idx =
                            _selectedPassengerRouteIndex.value.coerceIn(0, routeOptions.lastIndex.coerceAtLeast(0))
                        _passengerRouteOptions.value = routeOptions
                        _riderFareEstimate.value = routeOptions.getOrNull(idx)?.let { option ->
                            RiderFareEstimate(
                                straightLineKm = option.distanceKm,
                                distanceKm = option.distanceKm,
                                estimatedMinutes = option.estimatedMinutes,
                                fareTnd = option.fareTnd
                            )
                        }
                    }
                }
            }
        })
    }

    fun selectPassengerRoute(index: Int) {
        val routes = _passengerRouteGeometries.value
        if (index < 0 || index >= routes.size) return
        _selectedPassengerRouteIndex.value = index
        _passengerTrafficSpans.value = passengerTrafficByRoute.getOrNull(index).orEmpty()
        val options = _passengerRouteOptions.value
        _riderFareEstimate.value = options.getOrNull(index)?.let { option ->
            RiderFareEstimate(
                straightLineKm = option.distanceKm,
                distanceKm = option.distanceKm,
                estimatedMinutes = option.estimatedMinutes,
                fareTnd = option.fareTnd
            )
        }
    }

    private fun extractTrafficSpans(route: Route): List<PassengerTrafficSpan> {
        // Skip heavy traffic extraction for very long routes (urban / intercity cap).
        if ((route.lengthInMeters / 1000.0) > 150.0) return emptyList()
        if (!trafficSpanMethodsDebugLogged) {
            trafficSpanMethodsDebugLogged = true
            route.sections.firstOrNull()?.spans?.firstOrNull()?.let { span ->
                Log.d(TAG_ROUTE, "Span methods: ${span.javaClass.methods.map { it.name }}")
                span.dynamicSpeedInfo?.let { dsi ->
                    Log.d(TAG_ROUTE, "DynamicSpeedInfo methods: ${dsi.javaClass.methods.map { it.name }}")
                }
            }
        }
        val spans = ArrayList<PassengerTrafficSpan>()
        for (section in route.sections) {
            for (span in section.spans) {
                val jamFactorRaw = resolveJamFactor(span.dynamicSpeedInfo, span) ?: continue
                val jamFactor = normalizeJamFactor(jamFactorRaw) ?: continue
                if (jamFactor <= 3.0) continue
                val copied = copyGeometry(span.geometry) ?: continue
                spans.add(PassengerTrafficSpan(geometry = copied, jamFactor = jamFactor))
            }
        }
        return spans
    }

    /**
     * HERE SDKs can expose jam factor on different scales depending on platform/version.
     * - Some builds return 0..1
     * - Others return 0..10
     * We normalize to 0..10 for rendering consistency with Swift thresholds.
     */
    private fun normalizeJamFactor(raw: Double): Double? {
        if (!raw.isFinite() || raw < 0.0) return null
        return when {
            raw <= 1.0 -> raw * 10.0
            raw <= 10.0 -> raw
            raw <= 100.0 -> raw / 10.0
            else -> 10.0
        }.coerceIn(0.0, 10.0)
    }

    private fun resolveJamFactor(dynamicSpeedInfo: Any?, span: Any): Double? {
        // 1) Prefer official dynamicSpeedInfo jam factor (Swift equivalent).
        if (dynamicSpeedInfo != null) {
            runCatching {
                val m = dynamicSpeedInfo.javaClass.methods.firstOrNull {
                    it.name == "calculateJamFactor" && it.parameterCount == 0
                }
                val value = m?.invoke(dynamicSpeedInfo) as? Number
                if (value != null) return value.toDouble()
            }
            runCatching {
                val m = dynamicSpeedInfo.javaClass.methods.firstOrNull {
                    (it.name == "getJamFactor" || it.name == "jamFactor") && it.parameterCount == 0
                }
                val value = m?.invoke(dynamicSpeedInfo) as? Number
                if (value != null) return value.toDouble()
            }
            runCatching {
                val f = dynamicSpeedInfo.javaClass.declaredFields.firstOrNull { it.name == "jamFactor" } ?: return@runCatching
                f.isAccessible = true
                val value = f.get(dynamicSpeedInfo) as? Number
                if (value != null) return value.toDouble()
            }
        }

        // 2) Fallback: compute a pseudo jam factor from traffic/base speed on the span.
        val trafficSpeed = callNumberGetter(span, "getTrafficSpeedInMetersPerSecond")
            ?: callNumberGetter(span, "trafficSpeedInMetersPerSecond")
        val baseSpeed = callNumberGetter(span, "getBaseSpeedInMetersPerSecond")
            ?: callNumberGetter(span, "baseSpeedInMetersPerSecond")
            ?: callNumberGetter(span, "getSpeedLimitInMetersPerSecond")
            ?: callNumberGetter(span, "speedLimitInMetersPerSecond")
        if (trafficSpeed != null && baseSpeed != null && baseSpeed > 0.1) {
            val ratio = (trafficSpeed / baseSpeed).coerceIn(0.0, 1.0)
            val jam = ((1.0 - ratio) * 10.0).coerceIn(0.0, 10.0)
            return jam
        }
        return null
    }

    private fun callNumberGetter(target: Any, methodName: String): Double? =
        runCatching {
            val m = target.javaClass.methods.firstOrNull {
                it.name == methodName && it.parameterCount == 0
            } ?: return null
            (m.invoke(target) as? Number)?.toDouble()
        }.getOrNull()

    private fun copyGeometry(geometry: GeoPolyline): GeoPolyline? =
        try {
            val vertices = geometry.vertices
            if (vertices.isEmpty()) null else GeoPolyline(vertices)
        } catch (e: Exception) {
            Log.w(TAG_ROUTE, "copyGeometry: ${e.message}")
            null
        }

    fun applyPickupOverride(hit: AddressSearchHit) {
        clearPickupSearchResults()
        _pickupOverrideGeo.value = hit.coordinates
        _pickupOverrideLabel.value = hit.label
        updateRiderFareEstimateIfPossible(fromGps = false)
    }

    fun clearPickupOverride() {
        clearPickupSearchResults()
        _pickupOverrideGeo.value = null
        _pickupOverrideLabel.value = null
        updateRiderFareEstimateIfPossible(fromGps = false)
    }

    /**
     * Suggestions for the route sheet "From" field (same Geocoder stack as destination search).
     */
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
            delay(ADDRESS_SEARCH_DEBOUNCE_MS)
            if (!isActive) return@launch
            val list = forwardGeocode(trimmed)
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

    fun requestRide() {
        val pickup = effectivePickupGeo() ?: run {
            _rideRequestError.value = context.getString(R.string.map_error_pickup_location_missing)
            return
        }
        val destination = _confirmedDestination.value ?: run {
            _rideRequestError.value = context.getString(R.string.map_error_destination_missing)
            return
        }
        val pickupAddress = _pickupOverrideLabel.value ?: _currentAddress.value ?: run {
            _rideRequestError.value = context.getString(R.string.map_error_pickup_address_missing)
            return
        }
        val destinationAddress = _destinationLabel.value ?: run {
            _rideRequestError.value = context.getString(R.string.map_error_destination_address_missing)
            return
        }
        val routeOption = _passengerRouteOptions.value
            .getOrNull(_selectedPassengerRouteIndex.value)
            ?: _riderFareEstimate.value?.let {
                PassengerRouteOption(
                    distanceKm = it.distanceKm,
                    estimatedMinutes = it.estimatedMinutes,
                    fareTnd = it.fareTnd
                )
            }
            ?: run {
                _rideRequestError.value = context.getString(R.string.map_error_route_not_ready)
                return
            }

        val pickupNow = _ridePickupNow.value
        val forMe = _rideForMe.value
        val minScheduleMs = System.currentTimeMillis() + 30 * 60 * 1000L
        val scheduledAtIso = if (pickupNow) {
            null
        } else {
            val ms = (_rideScheduledAtEpochMs.value ?: (System.currentTimeMillis() + 45 * 60 * 1000L))
                .coerceAtLeast(minScheduleMs)
            formatRideScheduledAtUtc(ms)
        }
        if (!forMe) {
            val phone = _passengerBookingPhone.value.trim()
            if (phone.isEmpty()) {
                _rideRequestError.value = context.getString(R.string.map_error_passenger_phone_required)
                return
            }
        }

        viewModelScope.launch {
            _isRequestingRide.value = true
            _rideRequestError.value = null
            _lastRequestedRide.value = null
            _incomingRideOffers.value = emptyList()
            _pickingOfferId.value = null
            _matchedRideOffer.value = null
            _isRideMatched.value = false

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
                passengerName = _passengerBookingName.value.trim().takeIf { it.isNotEmpty() },
                passengerPhone = if (!forMe) _passengerBookingPhone.value.trim() else null
            ).onSuccess { ride ->
                _lastRequestedRide.value = ride
                if (ride.status == RideStatus.Completed) {
                    fetchRideRating(ride.id)
                }
                if (ride.status != RideStatus.Scheduled) {
                    startRideOffersPolling(ride.id)
                } else {
                    fetchScheduledRides()
                }
            }.onFailure { err ->
                _rideRequestError.value = err.message
                    ?: context.getString(R.string.map_error_request_ride_failed)
            }

            _isRequestingRide.value = false
        }
    }

    fun pickRideOffer(offerId: String) {
        val rideId = _lastRequestedRide.value?.id ?: return
        val selectedOffer = _incomingRideOffers.value.firstOrNull { it.id == offerId }
        viewModelScope.launch {
            _isRequestingRide.value = true
            _rideRequestError.value = null
            _pickingOfferId.value = offerId
            ridesRepository.pickRideOffer(rideId, offerId)
                .onSuccess {
                    stopRideOffersPolling()
                    _incomingRideOffers.value = emptyList()
                    _matchedRideOffer.value = selectedOffer
                    _isRideMatched.value = true
                    selectedOffer?.driverId?.let { fetchDriverRatings(it) }
                }
                .onFailure { err ->
                    _rideRequestError.value = err.message
                        ?: context.getString(R.string.map_error_pick_driver_failed)
                }
            _pickingOfferId.value = null
            _isRequestingRide.value = false
        }
    }

    fun cancelRequestedRide() {
        val rideId = _lastRequestedRide.value?.id ?: return
        viewModelScope.launch {
            _isRequestingRide.value = true
            _rideRequestError.value = null
            ridesRepository.cancelRideRequest(rideId, "rider_cancelled_from_offer_picker")
                .onSuccess {
                    stopRideOffersPolling()
                    _incomingRideOffers.value = emptyList()
                    _lastRequestedRide.value = null
                    _pickingOfferId.value = null
                    _matchedRideOffer.value = null
                    _isRideMatched.value = false
                    fetchScheduledRides()
                }
                .onFailure { err ->
                    _rideRequestError.value = err.message
                        ?: context.getString(R.string.map_error_cancel_ride_failed)
                }
            _isRequestingRide.value = false
        }
    }

    fun dismissIncomingOffer(offerId: String) {
        _incomingRideOffers.value = _incomingRideOffers.value.filterNot { it.id == offerId }
    }

    fun fetchScheduledRides() {
        viewModelScope.launch {
            _isLoadingScheduledRides.value = true
            _scheduledRidesError.value = null
            ridesRepository.getScheduledRides()
                .onSuccess { rides ->
                    _scheduledRides.value = rides
                        .filter { it.status == RideStatus.Scheduled }
                        .sortedBy { it.scheduledAt ?: "" }
                }
                .onFailure { err ->
                    _scheduledRidesError.value = err.message
                }
            _isLoadingScheduledRides.value = false
        }
    }

    fun fetchRideRating(rideId: String) {
        viewModelScope.launch {
            _isLoadingRideRating.value = true
            _rideRatingError.value = null
            ridesRepository.getRideRating(rideId)
                .onSuccess { rating -> _rideRating.value = rating }
                .onFailure { err ->
                    _rideRating.value = null
                    _rideRatingError.value = err.message
                }
            _isLoadingRideRating.value = false
        }
    }

    fun submitRideRating(rideId: String, score: Int, comment: String? = null) {
        if (score !in 1..5) return
        viewModelScope.launch {
            _isSubmittingRideRating.value = true
            _submitRideRatingError.value = null
            ridesRepository.submitRideRating(rideId = rideId, score = score, comment = comment)
                .onSuccess { rating -> _rideRating.value = rating }
                .onFailure { err ->
                    _submitRideRatingError.value = err.message
                }
            _isSubmittingRideRating.value = false
        }
    }

    fun fetchDriverRatings(driverId: String, page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            ridesRepository.getDriverRatings(driverId = driverId, page = page, limit = limit)
                .onSuccess { (_, stats) ->
                    _driverRatingsStats.value = stats
                }
                .onFailure { _ -> }
        }
    }

    fun updateDriverPreviewRoutes(driverLocation: GeoCoordinates?, activeRide: ActiveRide?) {
        if (driverLocation == null || activeRide == null) {
            driverPreviewRouteRequestId++
            _driverPreviewRouteGeometries.value = emptyList()
            return
        }
        val pickup = GeoCoordinates(activeRide.pickupLat, activeRide.pickupLng)
        val dropoff = GeoCoordinates(activeRide.dropoffLat, activeRide.dropoffLng)
        val requestId = ++driverPreviewRouteRequestId

        calculateRoutePolyline(driverLocation, pickup) { toPickup ->
            if (requestId != driverPreviewRouteRequestId) return@calculateRoutePolyline
            calculateRoutePolyline(pickup, dropoff) { pickupToDrop ->
                if (requestId != driverPreviewRouteRequestId) return@calculateRoutePolyline
                val routes = buildList {
                    toPickup?.let { add(it) }
                    pickupToDrop?.let { add(it) }
                }
                _driverPreviewRouteGeometries.value = routes
            }
        }
    }

    private fun calculateRoutePolyline(
        from: GeoCoordinates,
        to: GeoCoordinates,
        onDone: (GeoPolyline?) -> Unit
    ) {
        val waypoints = listOf(Waypoint(from), Waypoint(to))
        val options = CarOptions()
        routingEngine.calculateRoute(waypoints, options, object : CalculateRouteCallback {
            override fun onRouteCalculated(routingError: RoutingError?, routes: MutableList<Route>?) {
                if (routingError != null) {
                    viewModelScope.launch(Dispatchers.Main.immediate) { onDone(null) }
                    return
                }
                val geometry = routes
                    ?.firstOrNull()
                    ?.geometry
                    ?.let(::copyGeometry)
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    onDone(geometry)
                }
            }
        })
    }

    private fun startRideOffersPolling(rideId: String) {
        stopRideOffersPolling()
        rideOffersPollingJob = viewModelScope.launch {
            while (isActive) {
                _isLoadingRideOffers.value = true
                ridesRepository.getRideOffers(rideId)
                    .onSuccess { offers -> _incomingRideOffers.value = offers }
                    .onFailure { err ->
                        _rideRequestError.value = err.message
                            ?: context.getString(R.string.map_error_load_offers_failed)
                    }
                _isLoadingRideOffers.value = false
                delay(2500L)
            }
        }
    }

    private fun stopRideOffersPolling() {
        rideOffersPollingJob?.cancel()
        rideOffersPollingJob = null
        _isLoadingRideOffers.value = false
    }

    private suspend fun forwardGeocode(query: String): List<AddressSearchHit> =
        withContext(Dispatchers.IO) {
            val here = forwardGeocodeHereSuggest(query)
            if (here.isNotEmpty()) return@withContext here
            forwardGeocodeAndroid(query)
        }

    /** HERE SDK autosuggest — tuned for typing; biases around GPS or Tunis. */
    private suspend fun forwardGeocodeHereSuggest(query: String): List<AddressSearchHit> {
        val engine = searchEngine ?: return emptyList()
        val anchor = _currentLocation.value
            ?: GeoCoordinates(
                DEFAULT_SEARCH_BIAS_LAT,
                DEFAULT_SEARCH_BIAS_LNG
            )
        return suspendCancellableCoroutine { cont ->
            val textQuery = TextQuery(query, TextQuery.Area(anchor))
            val options = SearchOptions().apply {
                languageCode = searchLanguageCode()
                maxItems = 10
            }
            engine.suggest(
                textQuery,
                options,
                object : SuggestCallbackExtended {
                    override fun onSuggestExtendedCompleted(
                        searchError: SearchError?,
                        suggestions: MutableList<Suggestion>?,
                        responseDetails: ResponseDetails?
                    ) {
                        if (!cont.isActive) return
                        if (searchError != null) {
                            cont.resume(emptyList())
                            return
                        }
                        val hits = suggestions.orEmpty().mapNotNull { s ->
                            val place = s.place ?: return@mapNotNull null
                            val geo = place.geoCoordinates ?: return@mapNotNull null
                            val addrText = place.address?.addressText?.trim().orEmpty()
                            val label = addrText.takeIf { it.isNotBlank() }
                                ?: s.title.takeIf { !it.isNullOrBlank() } ?: return@mapNotNull null
                            AddressSearchHit(label = label, coordinates = geo)
                        }.distinctBy { "${it.coordinates.latitude},${it.coordinates.longitude}|${it.label}" }
                        cont.resume(hits)
                    }
                }
            )
        }
    }

    private fun searchLanguageCode(): LanguageCode =
        when (Locale.getDefault().language) {
            "fr" -> LanguageCode.FR_FR
            "ar" -> LanguageCode.AR_SA
            else -> LanguageCode.EN_US
        }

    private suspend fun forwardGeocodeAndroid(query: String): List<AddressSearchHit> {
        if (!Geocoder.isPresent()) return emptyList()
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val raw: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocationName(query, 5) { addresses ->
                        if (cont.isActive) cont.resume(addresses ?: emptyList())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 5) ?: emptyList()
            }
            raw.mapNotNull { addr ->
                val line = addr.toReadableString() ?: return@mapNotNull null
                val lat = addr.latitude
                val lon = addr.longitude
                if (lat == 0.0 && lon == 0.0) return@mapNotNull null
                AddressSearchHit(label = line, coordinates = GeoCoordinates(lat, lon))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun reverseGeocodeLine(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val line = addresses.firstOrNull()?.toReadableString()
                            if (cont.isActive) {
                                cont.resume(line)
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()
                        ?.toReadableString()
                }
            } catch (_: Exception) {
                null
            }
        }

    // ── Real-time LocationCallback ────────────────────────────────────────────

    private fun normalizeHeadingDegrees(degrees: Float): Float {
        var d = degrees % 360f
        if (d < 0f) d += 360f
        return d
    }

    private fun updateHeadingFromLocation(loc: Location) {
        val prevLat = lastSampleLat
        val prevLng = lastSampleLng
        lastSampleLat = loc.latitude
        lastSampleLng = loc.longitude

        val distM = FloatArray(1)
        val movedEnough = if (prevLat != null && prevLng != null) {
            Location.distanceBetween(prevLat, prevLng, loc.latitude, loc.longitude, distM)
            distM[0] >= MIN_HEADING_MOVE_METERS
        } else {
            false
        }

        val bearing = when {
            movedEnough && prevLat != null && prevLng != null -> {
                val origin = Location("").apply {
                    latitude = prevLat
                    longitude = prevLng
                }
                origin.bearingTo(loc)
            }
            loc.hasBearing() && loc.hasSpeed() && loc.speed >= MIN_HEADING_SPEED_MPS -> loc.bearing
            loc.hasBearing() && (!loc.hasSpeed() || loc.speed >= 0.12f) -> loc.bearing
            else -> null
        }
        if (bearing != null) {
            _locationHeadingDegrees.value = normalizeHeadingDegrees(bearing)
        }
    }

    private fun applyFusedLocationUpdate(loc: Location) {
        _currentLocation.value = GeoCoordinates(loc.latitude, loc.longitude)
        _locationAccuracy.value = loc.accuracy
        updateHeadingFromLocation(loc)
        geocodeLocation(loc.latitude, loc.longitude)
        updateRiderFareEstimateIfPossible(fromGps = true)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            // Every fix in the batch (oldest → newest); using only lastLocation often skips 2 m steps
            // needed for bearing when hasBearing() is false (common on some devices).
            for (i in 0 until result.locations.size) {
                applyFusedLocationUpdate(result.locations[i])
            }
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Start real-time GPS updates.
     * Matches Swift: desiredAccuracy = NearestTenMeters, distanceFilter = 8m
     * Android equivalent: BALANCED_POWER, interval 10s, min 5s
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (_isTracking.value) return

        // Last known location for immediate display
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let { applyFusedLocationUpdate(it) }
        }

        // High accuracy; distance must stay ≤ [MIN_HEADING_MOVE_METERS] so bearingTo can fire each update.
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
            .setMinUpdateIntervalMillis(1_000L)
            .setMinUpdateDistanceMeters(1f)
            .build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
        _isTracking.value = true
    }

    /** Stop GPS updates (save battery). */
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _isTracking.value = false
    }

    /** Fetch last known location without starting continuous updates. */
    @SuppressLint("MissingPermission")
    fun fetchLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let { applyFusedLocationUpdate(it) }
        }
    }

    // ── Reverse geocoding ────────────────────────────────────────────────────

    private fun geocodeLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        _currentAddress.value = addresses.firstOrNull()?.toReadableString()
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val formatted = addresses?.firstOrNull()?.toReadableString()
                    withContext(Dispatchers.Main) {
                        _currentAddress.value = formatted
                    }
                }
            } catch (_: Exception) {
                // Geocoder can fail without network or Play services
            }
        }
    }

    private fun formatRideScheduledAtUtc(epochMs: Long): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date(epochMs))
    }

    override fun onCleared() {
        super.onCleared()
        stopRideOffersPolling()
        quickFareJob?.cancel()
        stopLocationUpdates()
        passengerRouteRequestId++
        _passengerRouteGeometries.value = emptyList()
        _passengerTrafficSpans.value = emptyList()
        _passengerRouteOptions.value = emptyList()
        _selectedPassengerRouteIndex.value = 0
        passengerTrafficByRoute = emptyList()
        routeRefreshPickupAnchor = null
        routeRefreshDropAnchor = null
        routingEngine.dispose()
    }

    private companion object {
        private const val TAG_ROUTE = "PassengerRoute"
        /**
         * Minimum displacement between fixes for [Location.bearingTo].
         * Must be ≤ typical gap between fused updates (see [LocationRequest.setMinUpdateDistanceMeters]),
         * otherwise bearing stays null and the taxi icon never rotates.
         */
        private const val MIN_HEADING_MOVE_METERS = 1f
        /** Prefer fused [Location.getBearing] when speed is at least this (m/s). */
        private const val MIN_HEADING_SPEED_MPS = 0.5f
        /** GPS-only debounce (user-confirmed routes use 0 ms). */
        private const val PASSENGER_ROUTE_DEBOUNCE_MS = 600L
        private const val PASSENGER_ROUTE_GPS_RESCHEDULE_METERS = 10f
        /** Shorter wait + HERE suggest = faster suggestions than Geocoder-only + 400ms. */
        private const val ADDRESS_SEARCH_DEBOUNCE_MS = 220L
        private const val DEFAULT_SEARCH_BIAS_LAT = 36.8065
        private const val DEFAULT_SEARCH_BIAS_LNG = 10.1815
    }
}

/** Returns the first address line, or null if empty. */
private fun Address.toReadableString(): String? =
    getAddressLine(0)?.takeIf { it.isNotBlank() }