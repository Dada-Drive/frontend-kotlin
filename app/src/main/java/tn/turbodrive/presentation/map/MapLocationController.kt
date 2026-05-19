package tn.turbodrive.presentation.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.here.sdk.core.GeoCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tn.turbodrive.data.location.GpsMode
import tn.turbodrive.data.location.LocationServiceController
import java.util.Locale

/**
 * Fused location updates, heading, and reverse geocode for "current address" chip.
 */
internal class MapLocationController(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationServiceController: LocationServiceController,
    private val scope: CoroutineScope,
    private val currentLocation: MutableStateFlow<GeoCoordinates?>,
    private val locationAccuracy: MutableStateFlow<Float?>,
    private val locationHeadingDegrees: MutableStateFlow<Float?>,
    private val currentAddress: MutableStateFlow<String?>,
    private val isTracking: MutableStateFlow<Boolean>,
    private val onAfterFusedLocationFix: () -> Unit,
) {
    private var lastSampleLat: Double? = null
    private var lastSampleLng: Double? = null
    private var lastRawLocation: Location? = null

    private val locationCallback =
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (i in 0 until result.locations.size) {
                    applyFusedLocationUpdate(result.locations[i])
                }
            }
        }

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
        val movedEnough =
            if (prevLat != null && prevLng != null) {
                Location.distanceBetween(prevLat, prevLng, loc.latitude, loc.longitude, distM)
                distM[0] >= MapViewModelConstants.MIN_HEADING_MOVE_METERS
            } else {
                false
            }

        val bearing =
            when {
                movedEnough && prevLat != null && prevLng != null -> {
                    val origin =
                        Location("").apply {
                            latitude = prevLat
                            longitude = prevLng
                        }
                    origin.bearingTo(loc)
                }
                loc.hasBearing() && loc.hasSpeed() && loc.speed >= MapViewModelConstants.MIN_HEADING_SPEED_MPS -> loc.bearing
                loc.hasBearing() && (!loc.hasSpeed() || loc.speed >= 0.12f) -> loc.bearing
                else -> null
            }
        if (bearing != null) {
            locationHeadingDegrees.value = normalizeHeadingDegrees(bearing)
        }
    }

    private fun applyFusedLocationUpdate(loc: Location) {
        if (loc.accuracy > 50f) return
        val prev = lastRawLocation
        if (prev != null && loc.distanceTo(prev) < 8f) return
        lastRawLocation = loc
        currentLocation.value = GeoCoordinates(loc.latitude, loc.longitude)
        locationAccuracy.value = loc.accuracy
        updateHeadingFromLocation(loc)
        geocodeLocation(loc.latitude, loc.longitude)
        onAfterFusedLocationFix()
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(mode: GpsMode = GpsMode.COARSE) {
        if (isTracking.value) return

        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let { applyFusedLocationUpdate(it) }
        }

        val request = locationServiceController.buildRequest(mode) ?: return
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        isTracking.value = true
    }

    @SuppressLint("MissingPermission")
    fun applyGpsMode(mode: GpsMode) {
        if (!isTracking.value) return
        val request =
            locationServiceController.buildRequest(mode) ?: run {
                stopLocationUpdates()
                return
            }
        fusedLocationClient.removeLocationUpdates(locationCallback)
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isTracking.value = false
    }

    @SuppressLint("MissingPermission")
    fun fetchLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let { applyFusedLocationUpdate(it) }
        }
    }

    private fun geocodeLocation(
        latitude: Double,
        longitude: Double,
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        currentAddress.value = addresses.firstOrNull()?.toReadableString()
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val formatted = addresses?.firstOrNull()?.toReadableString()
                    withContext(Dispatchers.Main) {
                        currentAddress.value = formatted
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}
