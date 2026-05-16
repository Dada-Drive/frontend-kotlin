package tn.dadadrive.presentation.map

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
    const val ADDRESS_SEARCH_DEBOUNCE_MS = 120L

    /**
     * How long to wait before showing the spinner on a search that is still in flight.
     * Cache hits and very fast network roundtrips complete before this, so the UI
     * never shows a loading flicker for snappy results.
     */
    const val ADDRESS_SEARCH_LOADING_GRACE_MS = 140L

    /** LRU cache size for recent forward-geocode queries, indexed by lower-cased trimmed string. */
    const val ADDRESS_SEARCH_CACHE_SIZE = 64

    const val DEFAULT_SEARCH_BIAS_LAT = 36.8065
    const val DEFAULT_SEARCH_BIAS_LNG = 10.1815
}
