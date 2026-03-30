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
import com.here.sdk.core.GeoCoordinates
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Représente un marqueur personnalisé placé par l'utilisateur sur la carte.
 * Custom pin placed manually by the user on the map.
 */
data class DadaMarker(
    val id: String = UUID.randomUUID().toString(),
    val position: GeoCoordinates,
    val title: String,
    val snippet: String = ""
)

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // ── Position GPS ──────────────────────────────────────────────────────────

    private val _currentLocation = MutableStateFlow<GeoCoordinates?>(null)
    val currentLocation: StateFlow<GeoCoordinates?> = _currentLocation.asStateFlow()

    /** Précision GPS en mètres (rayon du cercle d'incertitude). */
    private val _locationAccuracy = MutableStateFlow<Float?>(null)
    val locationAccuracy: StateFlow<Float?> = _locationAccuracy.asStateFlow()

    /** Adresse textuelle de la position actuelle (via Geocoder). */
    private val _currentAddress = MutableStateFlow<String?>(null)
    val currentAddress: StateFlow<String?> = _currentAddress.asStateFlow()

    /** true quand les mises à jour GPS temps réel sont actives. */
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // ── Marqueurs utilisateur ─────────────────────────────────────────────────

    private val _markers = MutableStateFlow<List<DadaMarker>>(emptyList())
    val markers: StateFlow<List<DadaMarker>> = _markers.asStateFlow()

    // ── Point tapé (info-bulle coordonnées) ───────────────────────────────────

    private val _tappedPoint = MutableStateFlow<GeoCoordinates?>(null)
    val tappedPoint: StateFlow<GeoCoordinates?> = _tappedPoint.asStateFlow()

    // ── LocationCallback temps réel ───────────────────────────────────────────

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                _currentLocation.value = GeoCoordinates(loc.latitude, loc.longitude)
                _locationAccuracy.value = loc.accuracy
                geocodeLocation(loc.latitude, loc.longitude)
            }
        }
    }

    // ── API publique ──────────────────────────────────────────────────────────

    /**
     * Démarre les mises à jour GPS en temps réel.
     * Récupère d'abord la dernière position connue, puis s'abonne aux updates continues.
     * Interval : 5 s, interval minimum : 2 s.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (_isTracking.value) return

        // Dernière position connue pour un affichage immédiat
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let {
                _currentLocation.value = GeoCoordinates(it.latitude, it.longitude)
                _locationAccuracy.value = it.accuracy
                geocodeLocation(it.latitude, it.longitude)
            }
        }

        // Mises à jour continues (haute précision GPS)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_000L)
            .build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
        _isTracking.value = true
    }

    /** Arrête les mises à jour GPS (économise la batterie). */
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _isTracking.value = false
    }

    /** Récupère la dernière position connue, sans démarrer les mises à jour continues. */
    @SuppressLint("MissingPermission")
    fun fetchLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let {
                _currentLocation.value = GeoCoordinates(it.latitude, it.longitude)
                _locationAccuracy.value = it.accuracy
            }
        }
    }

    fun onMapTapped(point: GeoCoordinates) {
        _tappedPoint.value = point
    }

    fun dismissTappedPoint() {
        _tappedPoint.value = null
    }

    fun addMarker(position: GeoCoordinates, title: String, snippet: String = "") {
        _markers.value = _markers.value + DadaMarker(position = position, title = title, snippet = snippet)
    }

    fun removeMarker(id: String) {
        _markers.value = _markers.value.filter { it.id != id }
    }

    fun clearMarkers() {
        _markers.value = emptyList()
    }

    // ── Géocodage inverse ──────────────────────────────────────────────────────

    /**
     * Convertit des coordonnées GPS en adresse textuelle via Geocoder.
     * Exécuté sur Dispatchers.IO pour ne pas bloquer le thread principal.
     */
    private fun geocodeLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // API 33+ : callback asynchrone
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        _currentAddress.value = addresses.firstOrNull()?.toReadableString()
                    }
                } else {
                    // Avant API 33 : appel synchrone (sur IO dispatcher)
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val formatted = addresses?.firstOrNull()?.toReadableString()
                    withContext(Dispatchers.Main) {
                        _currentAddress.value = formatted
                    }
                }
            } catch (_: Exception) {
                // Geocoder peut échouer sans réseau ou service Play indisponible
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}

/** Retourne la première ligne d'adresse, ou null si vide. */
private fun Address.toReadableString(): String? =
    getAddressLine(0)?.takeIf { it.isNotBlank() }
