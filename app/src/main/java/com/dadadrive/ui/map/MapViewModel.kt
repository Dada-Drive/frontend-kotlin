package com.dadadrive.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.dadadrive.core.pricing.RideRouteEstimator
import com.dadadrive.core.pricing.RiderFareEstimate
import com.here.sdk.core.GeoCoordinates
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

data class AddressSearchHit(
    val label: String,
    val coordinates: GeoCoordinates
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // ── GPS Position ────────────────────────────────────────────────────────

    private val _currentLocation = MutableStateFlow<GeoCoordinates?>(null)
    val currentLocation: StateFlow<GeoCoordinates?> = _currentLocation.asStateFlow()

    /** GPS accuracy in metres (uncertainty circle radius). */
    private val _locationAccuracy = MutableStateFlow<Float?>(null)
    val locationAccuracy: StateFlow<Float?> = _locationAccuracy.asStateFlow()

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

    /** Forward-geocode suggestions for the bottom search field (Android Geocoder). */
    private val _addressSearchResults = MutableStateFlow<List<AddressSearchHit>>(emptyList())
    val addressSearchResults: StateFlow<List<AddressSearchHit>> = _addressSearchResults.asStateFlow()

    private val _addressSearchLoading = MutableStateFlow(false)
    val addressSearchLoading: StateFlow<Boolean> = _addressSearchLoading.asStateFlow()

    private val _pickupSearchResults = MutableStateFlow<List<AddressSearchHit>>(emptyList())
    val pickupSearchResults: StateFlow<List<AddressSearchHit>> = _pickupSearchResults.asStateFlow()

    private val _pickupSearchLoading = MutableStateFlow(false)
    val pickupSearchLoading: StateFlow<Boolean> = _pickupSearchLoading.asStateFlow()

    /** Pickup→drop estimate using same fare math as backend (see [com.dadadrive.core.pricing]). */
    private val _riderFareEstimate = MutableStateFlow<RiderFareEstimate?>(null)
    val riderFareEstimate: StateFlow<RiderFareEstimate?> = _riderFareEstimate.asStateFlow()

    private var pickGeocodeJob: Job? = null
    private var forwardSearchJob: Job? = null
    private var pickupForwardSearchJob: Job? = null

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
        updateRiderFareEstimateIfPossible()
    }

    fun clearConfirmedDestination() {
        _confirmedDestination.value = null
        _riderFareEstimate.value = null
    }

    fun updateDestinationLabelInput(text: String) {
        _destinationLabel.value = text.takeIf { it.isNotBlank() }
    }

    /**
     * Debounced forward geocode for typed queries. Results vary by device / Play Services / network.
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
            delay(400)
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
        updateRiderFareEstimateIfPossible()
    }

    private fun effectivePickupGeo(): GeoCoordinates? =
        _pickupOverrideGeo.value ?: _currentLocation.value

    private fun updateRiderFareEstimateIfPossible() {
        val pickup = effectivePickupGeo()
        val drop = _confirmedDestination.value
        if (pickup == null || drop == null) {
            _riderFareEstimate.value = null
            return
        }
        _riderFareEstimate.value = RideRouteEstimator.estimateFareFromPickupToDrop(pickup, drop)
    }

    fun applyPickupOverride(hit: AddressSearchHit) {
        clearPickupSearchResults()
        _pickupOverrideGeo.value = hit.coordinates
        _pickupOverrideLabel.value = hit.label
        updateRiderFareEstimateIfPossible()
    }

    fun clearPickupOverride() {
        clearPickupSearchResults()
        _pickupOverrideGeo.value = null
        _pickupOverrideLabel.value = null
        updateRiderFareEstimateIfPossible()
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
            delay(400)
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

    private suspend fun forwardGeocode(query: String): List<AddressSearchHit> =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext emptyList()
            try {
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

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                _currentLocation.value = GeoCoordinates(loc.latitude, loc.longitude)
                _locationAccuracy.value = loc.accuracy
                geocodeLocation(loc.latitude, loc.longitude)
                updateRiderFareEstimateIfPossible()
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
            loc?.let {
                _currentLocation.value = GeoCoordinates(it.latitude, it.longitude)
                _locationAccuracy.value = it.accuracy
                geocodeLocation(it.latitude, it.longitude)
                updateRiderFareEstimateIfPossible()
            }
        }

        // Continuous updates (balanced power = ~10m accuracy, like NearestTenMeters)
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
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
            loc?.let {
                _currentLocation.value = GeoCoordinates(it.latitude, it.longitude)
                _locationAccuracy.value = it.accuracy
                updateRiderFareEstimateIfPossible()
            }
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

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}

/** Returns the first address line, or null if empty. */
private fun Address.toReadableString(): String? =
    getAddressLine(0)?.takeIf { it.isNotBlank() }