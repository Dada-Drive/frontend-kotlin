package tn.dadadrive.presentation.map

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tn.dadadrive.core.pricing.RiderFareEstimate
import tn.dadadrive.data.local.ActiveRideDraftCache
import tn.dadadrive.domain.models.ActiveRide
import tn.dadadrive.domain.models.DriverRatingsStats
import tn.dadadrive.domain.models.NearbyTaxi
import tn.dadadrive.domain.models.PassengerRideOffer
import tn.dadadrive.domain.models.RideRating
import tn.dadadrive.domain.models.RideStop
import tn.dadadrive.domain.protocols.RidesRepository
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.routing.RoutingEngine
import com.here.sdk.search.Place
import com.here.sdk.search.SearchEngine
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val ridesRepository: RidesRepository,
    private val activeRideDraftCache: ActiveRideDraftCache,
) : ViewModel() {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // ── GPS Position ────────────────────────────────────────────────────────

    private val _currentLocation = MutableStateFlow<GeoCoordinates?>(null)
    val currentLocation: StateFlow<GeoCoordinates?> = _currentLocation.asStateFlow()

    private val _locationAccuracy = MutableStateFlow<Float?>(null)
    val locationAccuracy: StateFlow<Float?> = _locationAccuracy.asStateFlow()

    private val _locationHeadingDegrees = MutableStateFlow<Float?>(null)
    val locationHeadingDegrees: StateFlow<Float?> = _locationHeadingDegrees.asStateFlow()

    /**
     * Road-aware heading for the user taxi marker. When the driver is close enough to an
     * active route polyline ([ROUTE_HEADING_SNAP_METERS]), we return the tangent of the
     * nearest route segment so the car icon stays aligned with the road instead of
     * oscillating with noisy GPS bearing at low speed. Falls back to raw GPS heading
     * otherwise.
     */
    private val _effectiveHeadingDegrees = MutableStateFlow<Float?>(null)
    val effectiveHeadingDegrees: StateFlow<Float?> = _effectiveHeadingDegrees.asStateFlow()

    /**
     * Road-matched driver position for the taxi marker. When the raw GPS fix is within
     * [ROUTE_HEADING_SNAP_METERS] of an active route polyline we emit the projected
     * point onto that polyline — so the icon stays on the road instead of drifting on
     * both sides of it. Falls back to the raw GPS fix otherwise.
     */
    private val _snappedDriverLocation = MutableStateFlow<GeoCoordinates?>(null)
    val snappedDriverLocation: StateFlow<GeoCoordinates?> = _snappedDriverLocation.asStateFlow()

    private val _currentAddress = MutableStateFlow<String?>(null)
    val currentAddress: StateFlow<String?> = _currentAddress.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _nearbyTaxis = MutableStateFlow<List<NearbyTaxi>>(emptyList())
    val nearbyTaxis: StateFlow<List<NearbyTaxi>> = _nearbyTaxis.asStateFlow()

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

    private val _fitRouteToBoundsRequestId = MutableStateFlow(0)
    val fitRouteToBoundsRequestId: StateFlow<Int> = _fitRouteToBoundsRequestId.asStateFlow()

    private val _passengerRouteGeometries = MutableStateFlow<List<GeoPolyline>>(emptyList())
    val passengerRouteGeometries: StateFlow<List<GeoPolyline>> = _passengerRouteGeometries.asStateFlow()

    private val _passengerTrafficSpans = MutableStateFlow<List<PassengerTrafficSpan>>(emptyList())
    val passengerTrafficSpans: StateFlow<List<PassengerTrafficSpan>> = _passengerTrafficSpans.asStateFlow()

    private val _passengerRouteOptions = MutableStateFlow<List<PassengerRouteOption>>(emptyList())
    val passengerRouteOptions: StateFlow<List<PassengerRouteOption>> = _passengerRouteOptions.asStateFlow()

    private val _selectedPassengerRouteIndex = MutableStateFlow(0)
    val selectedPassengerRouteIndex: StateFlow<Int> = _selectedPassengerRouteIndex.asStateFlow()

    private val _poiResults = MutableStateFlow<List<Place>>(emptyList())
    val poiResults: StateFlow<List<Place>> = _poiResults.asStateFlow()

    private val _isPoiLoading = MutableStateFlow(false)
    val isPoiLoading: StateFlow<Boolean> = _isPoiLoading.asStateFlow()

    private val _selectedPoiCategory = MutableStateFlow<PoiCategory?>(null)
    val selectedPoiCategory: StateFlow<PoiCategory?> = _selectedPoiCategory.asStateFlow()

    private val _poiSearchField = MutableStateFlow<PoiSearchField?>(null)
    val poiSearchField: StateFlow<PoiSearchField?> = _poiSearchField.asStateFlow()

    private val _poiSelectionTarget = MutableStateFlow<PoiSelectionTarget?>(null)
    val poiSelectionTarget: StateFlow<PoiSelectionTarget?> = _poiSelectionTarget.asStateFlow()

    private val _tappedPoi = MutableStateFlow<Pair<Place, PoiCategory>?>(null)
    val tappedPoi: StateFlow<Pair<Place, PoiCategory>?> = _tappedPoi.asStateFlow()

    private val _poiSearchError = MutableStateFlow<String?>(null)
    val poiSearchError: StateFlow<String?> = _poiSearchError.asStateFlow()

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

    // ── Intermediate stops (rider-entered "stationnements d'arrêt") ─────────
    //
    // Validation rule (client side): each stop must lie within
    // [INTERMEDIATE_STOP_MAX_DIST_METERS] of the currently selected passenger
    // route polyline — otherwise the map shows a forbidden overlay pin. On ride
    // request we POST only the stops that are [IntermediateStopValidity.OnRoute]
    // to the backend (`POST /rides/:id/stops`).

    private val _intermediateStopDrafts = MutableStateFlow<List<IntermediateStopDraft>>(emptyList())
    val intermediateStopDrafts: StateFlow<List<IntermediateStopDraft>> =
        _intermediateStopDrafts.asStateFlow()

    /** Pending stop picked from map; -1 means "append new". */
    private val _intermediateStopPickerIndex = MutableStateFlow<Int?>(null)
    val intermediateStopPickerIndex: StateFlow<Int?> = _intermediateStopPickerIndex.asStateFlow()

    private var intermediateStopSearchJob: Job? = null

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
        intermediateStopCoordinatesProvider = {
            _intermediateStopDrafts.value.mapNotNull { it.coordinates }
        }
    )

    private val geocodingHelper = MapGeocodingHelper(
        context = context,
        searchEngine = searchEngine,
        getBiasAnchor = { _currentLocation.value }
    )

    private val poiSearchHelper = PoiSearchHelper(
        scope = viewModelScope,
        searchEngineProvider = { searchEngine }
    )
    private var lastPoiQueryKey: String? = null

    private val rideOps = MapRideOperations(
        context = context,
        scope = viewModelScope,
        ridesRepository = ridesRepository,
        activeRideDraftCache = activeRideDraftCache,
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
        pendingIntermediateStopsProvider = {
            _intermediateStopDrafts.value
                .filter { it.isReady() }
                .mapIndexedNotNull { index, draft ->
                    val geo = draft.coordinates ?: return@mapIndexedNotNull null
                    RideStop(
                        id = "",
                        rideId = "",
                        address = draft.label,
                        lat = geo.latitude,
                        lng = geo.longitude,
                        orderIndex = index + 1
                    )
                }
        },
        onIntermediateStopsPosted = {
            _intermediateStopDrafts.value = emptyList()
        }
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
            maybeFetchNearbyTaxisAfterGpsFix()
        }
    )

    private var pickGeocodeJob: Job? = null
    private var forwardSearchJob: Job? = null
    private var pickupForwardSearchJob: Job? = null
    private var nearbyTaxisPollingJob: Job? = null
    private var nearbyTaxisEnabled = true

    /** Throttles GPS-driven fetches so we do not hammer /driver/nearby on every 1–2 s fix. */
    private var lastNearbyTaxisFetchElapsedMs = 0L

    init {
        // Auto-revalidate every intermediate stop whenever the route geometry changes
        // (e.g. after a new pickup/destination is picked or a different route variant
        // is selected). Keeps the yellow/forbidden pin state in sync with the map.
        viewModelScope.launch {
            _passengerRouteGeometries.collect { polylines ->
                revalidateIntermediateStopDrafts(polylines)
                if (polylines.isNotEmpty()) {
                    _fitRouteToBoundsRequestId.value = _fitRouteToBoundsRequestId.value + 1
                }
            }
        }

        // Road-aware taxi heading + map-matched position. When the raw GPS fix is close
        // enough to an active route polyline we project it onto that polyline and take
        // the segment tangent as heading — that's what gives the Bolt/inDrive feel
        // (icon locked to the road, pointing along the current segment). Rider
        // `passengerRouteGeometries` is a last-resort source for the rare case where
        // the driver view is still bound to the passenger route.
        viewModelScope.launch {
            combine(
                _currentLocation,
                _locationHeadingDegrees,
                _driverPreviewRouteGeometries,
                _passengerRouteGeometries,
            ) { loc, gpsHeading, driverRoutes, passengerRoutes ->
                computeMapMatch(loc, gpsHeading, driverRoutes, passengerRoutes)
            }.collect { (snappedLocation, heading) ->
                _snappedDriverLocation.value = snappedLocation
                _effectiveHeadingDegrees.value = heading
            }
        }
    }

    private fun computeMapMatch(
        location: GeoCoordinates?,
        gpsHeading: Float?,
        driverRoutes: List<GeoPolyline>,
        passengerRoutes: List<GeoPolyline>,
    ): Pair<GeoCoordinates?, Float?> {
        if (location == null) return null to gpsHeading
        val activeRoutes = if (driverRoutes.isNotEmpty()) driverRoutes else passengerRoutes
        if (activeRoutes.isEmpty()) return location to gpsHeading

        val match = RouteHeadingSnapper.match(
            position = location,
            polylines = activeRoutes,
            referenceHeadingDeg = gpsHeading,
            maxSnapMeters = ROUTE_HEADING_SNAP_METERS,
        )
        return if (match != null) {
            match.position to match.bearingDeg
        } else {
            location to gpsHeading
        }
    }

    private companion object {
        private const val ROUTE_HEADING_SNAP_METERS = 30.0
        private const val NEARBY_TAXIS_MIN_FETCH_INTERVAL_MS = 4_000L
    }

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
        // Efface l’adresse jusqu’au reverse geocode : l’overlay affiche le chargement et peut vibrer à l’arrivée.
        _pickTargetAddress.value = null
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

    fun setPickupOverrideFromCurrentLocation() {
        val geo = _currentLocation.value ?: return
        _pickupOverrideGeo.value = geo
        _pickupOverrideLabel.value = _currentAddress.value
            ?: String.format(Locale.US, "%.5f, %.5f", geo.latitude, geo.longitude)
        passengerRouting.updateRiderFareEstimateIfPossible(fromGps = false)
    }

    fun primePickupPickerFromCurrentLocation() {
        val geo = _currentLocation.value ?: return
        _pickTargetGeo.value = geo
        _pickTargetAddress.value = _currentAddress.value
            ?: String.format(Locale.US, "%.5f, %.5f", geo.latitude, geo.longitude)
    }

    fun clearConfirmedDestination() {
        rideOps.stopRideOffersPolling()
        rideOps.stopRideStatusPolling()
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
        geocodingHelper.cachedForwardGeocode(trimmed)?.let { cached ->
            _addressSearchResults.value = cached
            _addressSearchLoading.value = false
            return
        }
        forwardSearchJob = viewModelScope.launch {
            delay(MapViewModelConstants.ADDRESS_SEARCH_DEBOUNCE_MS)
            if (!isActive) return@launch
            val loadingReveal = launch {
                delay(MapViewModelConstants.ADDRESS_SEARCH_LOADING_GRACE_MS)
                if (isActive) _addressSearchLoading.value = true
            }
            val list = geocodingHelper.forwardGeocode(trimmed)
            loadingReveal.cancel()
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

    fun onSearchQueryChanged(query: String, field: PoiSearchField) {
        _poiSearchField.value = field
        val trimmed = query.trim()
        if (trimmed.isBlank() || trimmed.length < 3) {
            clearPoiResults()
            return
        }
        val category = detectCategory(trimmed)
        if (category == null) {
            clearPoiResults()
            return
        }
        val queryKey = "${field.name}|${category.name}|${trimmed.lowercase(Locale.ROOT)}"
        if (queryKey == lastPoiQueryKey && _poiResults.value.isNotEmpty()) return
        lastPoiQueryKey = queryKey
        // Fallback Grand Tunis si GPS absent.
        val userLocation = _currentLocation.value
            ?: GeoCoordinates(36.8065, 10.1815)
        _selectedPoiCategory.value = category
        _isPoiLoading.value = true
        poiSearchHelper.searchByCategory(
            category = category,
            userLocation = userLocation,
            radius = 35000,
            maxResults = 80,
            onSuccess = { places ->
                _isPoiLoading.value = false
                _poiResults.value = places
                _selectedPoiCategory.value = category
            },
            onError = { message ->
                _isPoiLoading.value = false
                _poiResults.value = emptyList()
                _poiSearchError.value = message
            }
        )
    }

    fun clearPoiResults() {
        _poiResults.value = emptyList()
        _isPoiLoading.value = false
        _selectedPoiCategory.value = null
        _poiSearchField.value = null
        _poiSelectionTarget.value = null
        lastPoiQueryKey = null
    }

    fun triggerPoiCategorySearch(category: PoiCategory, target: PoiSelectionTarget) {
        if (category == PoiCategory.CUSTOM_LOCATION || category.hereCategoryId.isBlank()) return
        _poiSelectionTarget.value = target
        _poiSearchField.value = when (target) {
            is PoiSelectionTarget.Pickup -> PoiSearchField.PICKUP
            is PoiSelectionTarget.Destination, is PoiSelectionTarget.IntermediateStop -> PoiSearchField.DROPOFF
        }
        val userLocation = _currentLocation.value ?: GeoCoordinates(36.8065, 10.1815)
        val targetKey = when (target) {
            is PoiSelectionTarget.Pickup -> "P"
            is PoiSelectionTarget.Destination -> "D"
            is PoiSelectionTarget.IntermediateStop -> "I${target.index}"
        }
        val queryKey = "trg|$targetKey|${category.name}"
        if (queryKey == lastPoiQueryKey && _poiResults.value.isNotEmpty()) return
        lastPoiQueryKey = queryKey
        _selectedPoiCategory.value = category
        _isPoiLoading.value = true
        poiSearchHelper.searchByCategory(
            category = category,
            userLocation = userLocation,
            radius = 35000,
            maxResults = 80,
            onSuccess = { places ->
                _isPoiLoading.value = false
                _poiResults.value = places
                _selectedPoiCategory.value = category
            },
            onError = { message ->
                _isPoiLoading.value = false
                _poiResults.value = emptyList()
                _poiSearchError.value = message
            }
        )
    }

    fun onPoiMarkerTapped(place: Place, category: PoiCategory) {
        _tappedPoi.value = place to category
    }

    fun clearTappedPoi() {
        _tappedPoi.value = null
    }

    fun setPickupFromPoi(place: Place) {
        val geo = place.geoCoordinates ?: return
        applyPickupOverride(AddressSearchHit(place.title, geo))
        clearPoiResults()
    }

    fun setDropoffFromPoi(place: Place) {
        val geo = place.geoCoordinates ?: return
        applySearchDestination(AddressSearchHit(place.title, geo))
        clearPoiResults()
    }

    fun setIntermediateStopFromPoi(place: Place) {
        val idx = (_poiSelectionTarget.value as? PoiSelectionTarget.IntermediateStop)?.index ?: return
        val geo = place.geoCoordinates ?: return
        applyIntermediateStopHit(idx, AddressSearchHit(place.title, geo))
        clearPoiResults()
    }

    fun consumePoiSearchError() {
        _poiSearchError.value = null
    }

    fun applySearchDestination(hit: AddressSearchHit) {
        clearAddressSearchResults()
        _confirmedDestination.value = hit.coordinates
        _destinationLabel.value = hit.label
        passengerRouting.updateRiderFareEstimateIfPossible(fromGps = false)
    }

    fun selectPassengerRoute(index: Int) {
        passengerRouting.selectPassengerRoute(index)
        if (_passengerRouteGeometries.value.isNotEmpty()) {
            _fitRouteToBoundsRequestId.value = _fitRouteToBoundsRequestId.value + 1
        }
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
        geocodingHelper.cachedForwardGeocode(trimmed)?.let { cached ->
            _pickupSearchResults.value = cached
            _pickupSearchLoading.value = false
            return
        }
        pickupForwardSearchJob = viewModelScope.launch {
            delay(MapViewModelConstants.ADDRESS_SEARCH_DEBOUNCE_MS)
            if (!isActive) return@launch
            val loadingReveal = launch {
                delay(MapViewModelConstants.ADDRESS_SEARCH_LOADING_GRACE_MS)
                if (isActive) _pickupSearchLoading.value = true
            }
            val list = geocodingHelper.forwardGeocode(trimmed)
            loadingReveal.cancel()
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

    // ── Intermediate stops ───────────────────────────────────────────────────

    fun addIntermediateStopDraft() {
        _intermediateStopDrafts.update { current ->
            current + IntermediateStopDraft(
                label = "",
                coordinates = null,
                validity = IntermediateStopValidity.Unknown
            )
        }
    }

    fun removeIntermediateStopDraft(index: Int) {
        _intermediateStopDrafts.update { current ->
            if (index !in current.indices) current else current.toMutableList().apply { removeAt(index) }
        }
        passengerRouting.schedulePassengerRouteRefresh(debounceMs = 0L)
    }

    fun updateIntermediateStopLabel(index: Int, label: String) {
        _intermediateStopDrafts.update { current ->
            if (index !in current.indices) return@update current
            val existing = current[index]
            // Typing clears the previously resolved coordinates until the user picks a suggestion.
            val cleared = if (label != existing.label) existing.copy(
                label = label,
                coordinates = null,
                validity = IntermediateStopValidity.Unknown
            ) else existing
            current.toMutableList().also { it[index] = cleared }
        }
        // Trigger forward-search shared with the destination list.
        scheduleIntermediateStopSearch(label)
        passengerRouting.schedulePassengerRouteRefresh(debounceMs = 0L)
    }

    fun applyIntermediateStopHit(index: Int, hit: AddressSearchHit) {
        clearAddressSearchResults()
        _intermediateStopDrafts.update { current ->
            if (index !in current.indices) return@update current
            val validity = evaluateStopValidity(hit.coordinates, _passengerRouteGeometries.value)
            current.toMutableList().also {
                it[index] = IntermediateStopDraft(
                    label = hit.label,
                    coordinates = hit.coordinates,
                    validity = validity
                )
            }
        }
        passengerRouting.schedulePassengerRouteRefresh(debounceMs = 0L)
    }

    /** Starts picking a stop on the map for [index] (or a new stop when [append] is true). */
    fun beginIntermediateStopMapPick(index: Int, append: Boolean) {
        if (append) {
            addIntermediateStopDraft()
            _intermediateStopPickerIndex.value = _intermediateStopDrafts.value.lastIndex
        } else {
            _intermediateStopPickerIndex.value = index.takeIf { it >= 0 }
        }
    }

    fun cancelIntermediateStopMapPick() {
        val idx = _intermediateStopPickerIndex.value
        if (idx != null) {
            _intermediateStopDrafts.update { current ->
                if (idx !in current.indices) return@update current
                val draft = current[idx]
                if (draft.label.isBlank() && draft.coordinates == null) {
                    current.toMutableList().apply { removeAt(idx) }
                } else {
                    current
                }
            }
        }
        _intermediateStopPickerIndex.value = null
    }

    /** Commits the current map-picker target as the stop at [intermediateStopPickerIndex]. */
    fun confirmIntermediateStopFromPicker() {
        val idx = _intermediateStopPickerIndex.value ?: return
        val geo = _pickTargetGeo.value ?: return
        val label = _pickTargetAddress.value
            ?: String.format(Locale.US, "%.5f, %.5f", geo.latitude, geo.longitude)
        _intermediateStopDrafts.update { current ->
            if (idx !in current.indices) return@update current
            val validity = evaluateStopValidity(geo, _passengerRouteGeometries.value)
            current.toMutableList().also {
                it[idx] = IntermediateStopDraft(
                    label = label,
                    coordinates = geo,
                    validity = validity
                )
            }
        }
        _intermediateStopPickerIndex.value = null
        passengerRouting.schedulePassengerRouteRefresh(debounceMs = 0L)
    }

    /**
     * Revalidates every stop draft against the supplied polylines. Called both
     * when the route recomputes and when the user picks a new stop.
     */
    private fun revalidateIntermediateStopDrafts(polylines: List<GeoPolyline>) {
        val current = _intermediateStopDrafts.value
        if (current.isEmpty()) return
        val next = current.map { draft ->
            val validity = evaluateStopValidity(draft.coordinates, polylines)
            if (validity == draft.validity) draft else draft.copy(validity = validity)
        }
        if (next != current) _intermediateStopDrafts.value = next
    }

    fun clearIntermediateStopDrafts() {
        _intermediateStopDrafts.value = emptyList()
        _intermediateStopPickerIndex.value = null
        passengerRouting.schedulePassengerRouteRefresh(debounceMs = 0L)
    }

    private fun scheduleIntermediateStopSearch(query: String) {
        // Stops share the destination search results channel — cheaper than another StateFlow
        // and the UI only shows one suggestion list at a time (focused field wins).
        scheduleAddressSearch(query)
    }

    fun requestRide() = rideOps.requestRide()

    fun clearRideRequestError() {
        _rideRequestError.value = null
    }

    fun pickRideOffer(offerId: String) = rideOps.pickRideOffer(offerId)

    fun cancelRequestedRide() = rideOps.cancelRequestedRide()

    fun dismissIncomingOffer(offerId: String) = rideOps.dismissIncomingOffer(offerId)

    fun fetchScheduledRides() = rideOps.fetchScheduledRides()

    fun fetchRideRating(rideId: String) = rideOps.fetchRideRating(rideId)

    fun submitRideRating(rideId: String, score: Int, comment: String? = null) =
        rideOps.submitRideRating(rideId, score, comment)

    fun fetchDriverRatings(driverId: String, page: Int = 1, limit: Int = 20) =
        rideOps.fetchDriverRatings(driverId, page, limit)

    fun updateDriverPreviewRoutes(
        driverLocation: GeoCoordinates?,
        activeRide: ActiveRide?,
        includeDropoffLeg: Boolean = true
    ) {
        driverPreviewRouting.updateDriverPreviewRoutes(
            driverLocation = driverLocation,
            activeRide = activeRide,
            driverPreviewRouteGeometries = _driverPreviewRouteGeometries,
            includeDropoffLeg = includeDropoffLeg
        )
    }

    fun fetchNearbyTaxis(radiusKm: Double = 10.0) {
        val location = _currentLocation.value ?: return
        viewModelScope.launch {
            ridesRepository.getNearbyTaxis(
                lat = location.latitude,
                lng = location.longitude,
                radiusKm = radiusKm
            ).onSuccess { taxis ->
                _nearbyTaxis.value = taxis
            }
        }
    }

    private fun maybeFetchNearbyTaxisAfterGpsFix() {
        if (!nearbyTaxisEnabled) return
        val now = SystemClock.elapsedRealtime()
        val interval = NEARBY_TAXIS_MIN_FETCH_INTERVAL_MS
        if (lastNearbyTaxisFetchElapsedMs != 0L &&
            now - lastNearbyTaxisFetchElapsedMs < interval
        ) {
            return
        }
        lastNearbyTaxisFetchElapsedMs = now
        fetchNearbyTaxis()
    }

    private fun startNearbyTaxisPolling() {
        if (!nearbyTaxisEnabled) return
        if (nearbyTaxisPollingJob?.isActive == true) return
        nearbyTaxisPollingJob = viewModelScope.launch {
            while (isActive) {
                fetchNearbyTaxis()
                delay(10_000L)
            }
        }
    }

    private fun stopNearbyTaxisPolling() {
        nearbyTaxisPollingJob?.cancel()
        nearbyTaxisPollingJob = null
        lastNearbyTaxisFetchElapsedMs = 0L
        _nearbyTaxis.value = emptyList()
    }

    fun startLocationUpdates(startNearbyPolling: Boolean = true) {
        nearbyTaxisEnabled = startNearbyPolling
        locationController.startLocationUpdates()
        if (startNearbyPolling) {
            startNearbyTaxisPolling()
        } else {
            stopNearbyTaxisPolling()
        }
    }

    suspend fun persistOnProcessLifecycleStop() {
        stopLocationUpdates()
        activeRideDraftCache.saveActiveOrClear(_lastRequestedRide.value)
    }

    fun stopLocationUpdates() {
        locationController.stopLocationUpdates()
        stopNearbyTaxisPolling()
    }

    fun fetchLastLocation() = locationController.fetchLastLocation()

    override fun onCleared() {
        super.onCleared()
        stopNearbyTaxisPolling()
        rideOps.stopRideOffersPolling()
        locationController.stopLocationUpdates()
        passengerRouting.onViewModelCleared()
        routingEngine.dispose()
    }
}
