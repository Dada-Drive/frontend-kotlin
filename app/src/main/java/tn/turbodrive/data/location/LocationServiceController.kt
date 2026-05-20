package tn.turbodrive.data.location

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class GpsMode { HIGH, COARSE, OFF }

@Singleton
class LocationServiceController
    @Inject
    constructor(
        @ApplicationContext @Suppress("UnusedPrivateMember") private val context: Context,
    ) {
        private val _gpsMode = MutableStateFlow(GpsMode.COARSE)
        val gpsMode: StateFlow<GpsMode> = _gpsMode.asStateFlow()

        private var lastAcceptedLocation: Location? = null

        fun setMode(newMode: GpsMode) {
            if (_gpsMode.value == newMode) return
            _gpsMode.value = newMode
        }

        fun shouldAcceptLocation(loc: Location): Boolean {
            if (_gpsMode.value == GpsMode.OFF) return false
            if (loc.accuracy > ACCURACY_MAX_M) return false
            val prev = lastAcceptedLocation
            if (prev != null && loc.distanceTo(prev) < MIN_DISPLACEMENT_M) return false
            lastAcceptedLocation = loc
            return true
        }

        fun resetLastLocation() {
            lastAcceptedLocation = null
        }

        fun buildRequest(mode: GpsMode): LocationRequest? =
            when (mode) {
                GpsMode.HIGH ->
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_HIGH_MS)
                        .setMinUpdateIntervalMillis(MIN_INTERVAL_HIGH_MS)
                        .setMinUpdateDistanceMeters(MIN_DISTANCE_HIGH_M)
                        .build()
                GpsMode.COARSE ->
                    LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, INTERVAL_COARSE_MS)
                        .setMinUpdateIntervalMillis(MIN_INTERVAL_COARSE_MS)
                        .setMinUpdateDistanceMeters(MIN_DISTANCE_COARSE_M)
                        .build()
                GpsMode.OFF -> null
            }

        private companion object {
            const val ACCURACY_MAX_M = 50f
            const val MIN_DISPLACEMENT_M = 8f
            const val INTERVAL_HIGH_MS = 2_000L
            const val MIN_INTERVAL_HIGH_MS = 1_000L
            const val MIN_DISTANCE_HIGH_M = 1f
            const val INTERVAL_COARSE_MS = 10_000L
            const val MIN_INTERVAL_COARSE_MS = 5_000L
            const val MIN_DISTANCE_COARSE_M = 50f
        }
    }
