package com.dadadrive.ui.map

internal object MapViewModelConstants {
    const val TAG_ROUTE = "PassengerRoute"

    /**
     * Minimum displacement between fixes for [android.location.Location.bearingTo].
     * Must be ≤ typical gap between fused updates, otherwise bearing stays null.
     */
    const val MIN_HEADING_MOVE_METERS = 1f

    /** Prefer fused [android.location.Location.getBearing] when speed is at least this (m/s). */
    const val MIN_HEADING_SPEED_MPS = 0.5f

    /** GPS-only debounce (user-confirmed routes use 0 ms). */
    const val PASSENGER_ROUTE_DEBOUNCE_MS = 600L

    const val PASSENGER_ROUTE_GPS_RESCHEDULE_METERS = 10f

    /** Shorter wait + HERE suggest = faster suggestions than Geocoder-only + 400ms. */
    const val ADDRESS_SEARCH_DEBOUNCE_MS = 220L

    const val DEFAULT_SEARCH_BIAS_LAT = 36.8065
    const val DEFAULT_SEARCH_BIAS_LNG = 10.1815
}
