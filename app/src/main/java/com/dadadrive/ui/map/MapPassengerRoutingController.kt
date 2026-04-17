package com.dadadrive.ui.map

import android.location.Location
import android.util.Log
import com.dadadrive.core.pricing.RideRouteEstimator
import com.dadadrive.core.pricing.RiderFareEstimate
import com.dadadrive.domain.repository.RidesRepository
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.routing.CalculateRouteCallback
import com.here.sdk.routing.CarOptions
import com.here.sdk.routing.Route
import com.here.sdk.routing.RoutingEngine
import com.here.sdk.routing.RoutingError
import com.here.sdk.routing.Waypoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * HERE passenger pickup→drop routes, quick fare fallback, and traffic-aware polylines.
 */
internal class MapPassengerRoutingController(
    private val scope: CoroutineScope,
    private val routingEngine: RoutingEngine,
    private val ridesRepository: RidesRepository,
    private val pickupOverride: MutableStateFlow<GeoCoordinates?>,
    private val currentLocation: MutableStateFlow<GeoCoordinates?>,
    private val confirmedDestination: MutableStateFlow<GeoCoordinates?>,
    private val riderFareEstimate: MutableStateFlow<RiderFareEstimate?>,
    private val passengerRouteGeometries: MutableStateFlow<List<GeoPolyline>>,
    private val passengerTrafficSpans: MutableStateFlow<List<PassengerTrafficSpan>>,
    private val passengerRouteOptions: MutableStateFlow<List<PassengerRouteOption>>,
    private val selectedPassengerRouteIndex: MutableStateFlow<Int>,
) {
    private var passengerRouteRequestId: Int = 0
    private var passengerRouteRefreshJob: Job? = null
    private var passengerTrafficByRoute: List<List<PassengerTrafficSpan>> = emptyList()
    private var routeRefreshPickupAnchor: GeoCoordinates? = null
    private var routeRefreshDropAnchor: GeoCoordinates? = null
    private var quickFareJob: Job? = null

    fun effectivePickupGeo(): GeoCoordinates? =
        pickupOverride.value ?: currentLocation.value

    fun cancelQuickFareJob() {
        quickFareJob?.cancel()
    }

    /**
     * Clears route + fare state when destination is cleared or [onViewModelCleared].
     */
    fun clearPassengerRouteState() {
        quickFareJob?.cancel()
        riderFareEstimate.value = null
        passengerRouteGeometries.value = emptyList()
        passengerTrafficSpans.value = emptyList()
        passengerRouteOptions.value = emptyList()
        selectedPassengerRouteIndex.value = 0
        passengerTrafficByRoute = emptyList()
        passengerRouteRequestId++
        passengerRouteRefreshJob?.cancel()
        routeRefreshPickupAnchor = null
        routeRefreshDropAnchor = null
    }

    fun onViewModelCleared() {
        quickFareJob?.cancel()
        passengerRouteRequestId++
        passengerRouteGeometries.value = emptyList()
        passengerTrafficSpans.value = emptyList()
        passengerRouteOptions.value = emptyList()
        selectedPassengerRouteIndex.value = 0
        passengerTrafficByRoute = emptyList()
        routeRefreshPickupAnchor = null
        routeRefreshDropAnchor = null
    }

    /**
     * @param fromGps When true, only debounced HERE routing is scheduled if pickup moved enough from
     * the anchor; avoids cancelling in-flight routes on every location tick.
     */
    fun updateRiderFareEstimateIfPossible(fromGps: Boolean = false) {
        val pickup = effectivePickupGeo()
        val drop = confirmedDestination.value
        if (pickup == null || drop == null) {
            quickFareJob?.cancel()
            riderFareEstimate.value = null
            passengerRouteGeometries.value = emptyList()
            passengerTrafficSpans.value = emptyList()
            passengerRouteOptions.value = emptyList()
            selectedPassengerRouteIndex.value = 0
            passengerTrafficByRoute = emptyList()
            passengerRouteRequestId++
            passengerRouteRefreshJob?.cancel()
            routeRefreshPickupAnchor = null
            routeRefreshDropAnchor = null
            return
        }
        quickFareJob?.cancel()
        quickFareJob = scope.launch {
            val straight = RideRouteEstimator.haversineKm(pickup, drop)
            val (distanceKm, minutes) = RideRouteEstimator.estimateDistanceAndMinutes(straight)
            val fare = ridesRepository.getFareOrFallback(distanceKm, minutes)
            if (pickup != effectivePickupGeo() || drop != confirmedDestination.value) return@launch
            if (passengerRouteGeometries.value.isNotEmpty()) return@launch
            riderFareEstimate.value = RiderFareEstimate(
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

    fun schedulePassengerRouteRefresh(debounceMs: Long = MapViewModelConstants.PASSENGER_ROUTE_DEBOUNCE_MS) {
        passengerRouteRefreshJob?.cancel()
        passengerRouteRefreshJob = scope.launch {
            if (debounceMs > 0L) delay(debounceMs)
            val pickup = effectivePickupGeo() ?: return@launch
            val drop = confirmedDestination.value ?: return@launch
            val pAnchor = routeRefreshPickupAnchor
            val dAnchor = routeRefreshDropAnchor
            if (pAnchor != null && dAnchor != null &&
                distanceMeters(pickup, pAnchor) <= MapViewModelConstants.PASSENGER_ROUTE_GPS_RESCHEDULE_METERS &&
                distanceMeters(drop, dAnchor) <= MapViewModelConstants.PASSENGER_ROUTE_GPS_RESCHEDULE_METERS
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
        if (distanceMeters(anchor, currentPickup) > MapViewModelConstants.PASSENGER_ROUTE_GPS_RESCHEDULE_METERS) {
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
                alternatives = 2
            }
        }
        routingEngine.calculateRoute(waypoints, options, object : CalculateRouteCallback {
            override fun onRouteCalculated(routingError: RoutingError?, routes: MutableList<Route>?) {
                if (requestId != passengerRouteRequestId) return
                if (routingError != null) {
                    Log.w(MapViewModelConstants.TAG_ROUTE, "calculateRoute: $routingError")
                    scope.launch(Dispatchers.Main) {
                        if (requestId == passengerRouteRequestId) {
                            routeRefreshPickupAnchor = null
                            routeRefreshDropAnchor = null
                            passengerRouteGeometries.value = emptyList()
                            passengerTrafficSpans.value = emptyList()
                            passengerRouteOptions.value = emptyList()
                            selectedPassengerRouteIndex.value = 0
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
                    scope.launch(Dispatchers.Main) {
                        if (requestId == passengerRouteRequestId) {
                            routeRefreshPickupAnchor = null
                            routeRefreshDropAnchor = null
                            passengerRouteGeometries.value = emptyList()
                            passengerTrafficSpans.value = emptyList()
                            passengerRouteOptions.value = emptyList()
                            selectedPassengerRouteIndex.value = 0
                            passengerTrafficByRoute = emptyList()
                        }
                    }
                    return
                }
                val defaultSelected = 0
                Log.d(
                    MapViewModelConstants.TAG_ROUTE,
                    "requestPassengerRouting: builtRoutes=${built.size}, show polylines ASAP; traffic + fares async"
                )
                scope.launch(Dispatchers.Main.immediate) {
                    if (requestId != passengerRouteRequestId) return@launch
                    val polylines = built.map { it.polyline }
                    val optimisticOptions = built.map { b ->
                        PassengerRouteOption(
                            distanceKm = b.distanceKm,
                            estimatedMinutes = b.minutes,
                            fareTnd = ridesRepository.localFareEstimate(b.distanceKm, b.minutes)
                        )
                    }
                    passengerRouteGeometries.value = polylines
                    passengerTrafficSpans.value = emptyList()
                    passengerRouteOptions.value = optimisticOptions
                    selectedPassengerRouteIndex.value = defaultSelected
                    passengerTrafficByRoute = List(built.size) { emptyList() }
                    riderFareEstimate.value = optimisticOptions.getOrNull(defaultSelected)?.let { option ->
                        RiderFareEstimate(
                            straightLineKm = option.distanceKm,
                            distanceKm = option.distanceKm,
                            estimatedMinutes = option.estimatedMinutes,
                            fareTnd = option.fareTnd
                        )
                    }
                }
                scope.launch(Dispatchers.Default) {
                    val trafficByRoute = routesSnapshot.map { extractTrafficSpans(it) }
                    withContext(Dispatchers.Main.immediate) {
                        if (requestId != passengerRouteRequestId) return@withContext
                        passengerTrafficByRoute = trafficByRoute
                        val idx = selectedPassengerRouteIndex.value
                            .coerceIn(0, trafficByRoute.lastIndex.coerceAtLeast(0))
                        passengerTrafficSpans.value = trafficByRoute.getOrNull(idx).orEmpty()
                    }
                }
                scope.launch(Dispatchers.IO) {
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
                            selectedPassengerRouteIndex.value.coerceIn(0, routeOptions.lastIndex.coerceAtLeast(0))
                        passengerRouteOptions.value = routeOptions
                        riderFareEstimate.value = routeOptions.getOrNull(idx)?.let { option ->
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
        val routes = passengerRouteGeometries.value
        if (index < 0 || index >= routes.size) return
        selectedPassengerRouteIndex.value = index
        passengerTrafficSpans.value = passengerTrafficByRoute.getOrNull(index).orEmpty()
        val options = passengerRouteOptions.value
        riderFareEstimate.value = options.getOrNull(index)?.let { option ->
            RiderFareEstimate(
                straightLineKm = option.distanceKm,
                distanceKm = option.distanceKm,
                estimatedMinutes = option.estimatedMinutes,
                fareTnd = option.fareTnd
            )
        }
    }

    private fun extractTrafficSpans(route: Route): List<PassengerTrafficSpan> =
        MapTrafficUtils.extractTrafficSpans(route, MapViewModelConstants.TAG_ROUTE)

    private fun copyGeometry(geometry: GeoPolyline): GeoPolyline? =
        MapTrafficUtils.copyGeometry(geometry, MapViewModelConstants.TAG_ROUTE)
}
