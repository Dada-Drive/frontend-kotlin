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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

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

    // ── Destination (choix unique sur la carte) ───────────────────────────────
    /** Point géo sous le centre de l’écran (mode choix destination sur la carte). */
    private val _pickTargetGeo = MutableStateFlow<GeoCoordinates?>(null)
    val pickTargetGeo: StateFlow<GeoCoordinates?> = _pickTargetGeo.asStateFlow()

    /** Adresse affichée au-dessus du pin (géocodage inverse du centre). */
    private val _pickTargetAddress = MutableStateFlow<String?>(null)
    val pickTargetAddress: StateFlow<String?> = _pickTargetAddress.asStateFlow()

    /** Destination confirmée (une seule) après « Terminer » sur la carte. */
    private val _confirmedDestination = MutableStateFlow<GeoCoordinates?>(null)
    val confirmedDestination: StateFlow<GeoCoordinates?> = _confirmedDestination.asStateFlow()

    /** Texte affiché dans le champ « À » de la feuille d’itinéraire. */
    private val _destinationLabel = MutableStateFlow<String?>(null)
    val destinationLabel: StateFlow<String?> = _destinationLabel.asStateFlow()

    private var pickGeocodeJob: Job? = null

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
            val line = reverseGeocodeLine(geo.latitude, geo.longitude)
            _pickTargetAddress.value = line
        }
    }

    fun confirmDestination() {
        val geo = _pickTargetGeo.value ?: return
        _confirmedDestination.value = geo
        _destinationLabel.value = _pickTargetAddress.value
            ?: String.format(Locale.US, "%.5f, %.5f", geo.latitude, geo.longitude)
    }

    fun clearConfirmedDestination() {
        _confirmedDestination.value = null
    }

    fun updateDestinationLabelInput(text: String) {
        _destinationLabel.value = text.takeIf { it.isNotBlank() }
    }

    private suspend fun reverseGeocodeLine(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val line = addresses.firstOrNull()?.toReadableString()
                            // Ne jamais resume après cancel (ex. Terminer → resetPickerDraft) : sinon crash sur le thread du Geocoder.
                            if (cont.isActive) {
                                cont.resume(line)
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.toReadableString()
                }
            } catch (_: Exception) {
                null
            }
        }

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
