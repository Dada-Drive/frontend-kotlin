package com.dadadrive.ui.map

import com.dadadrive.domain.model.ActiveRide
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Driver map: preview polyline from current location → pickup → dropoff for an active ride.
 */
internal class MapDriverPreviewRouting(
    private val scope: CoroutineScope,
    private val routingEngine: RoutingEngine
) {
    private var driverPreviewRouteRequestId: Int = 0

    fun updateDriverPreviewRoutes(
        driverLocation: GeoCoordinates?,
        activeRide: ActiveRide?,
        driverPreviewRouteGeometries: MutableStateFlow<List<GeoPolyline>>
    ) {
        if (driverLocation == null || activeRide == null) {
            driverPreviewRouteRequestId++
            driverPreviewRouteGeometries.value = emptyList()
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
                driverPreviewRouteGeometries.value = routes
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
                    scope.launch(Dispatchers.Main.immediate) { onDone(null) }
                    return
                }
                val geometry = routes
                    ?.firstOrNull()
                    ?.geometry
                    ?.let { MapTrafficUtils.copyGeometry(it, MapViewModelConstants.TAG_ROUTE) }
                scope.launch(Dispatchers.Main.immediate) {
                    onDone(geometry)
                }
            }
        })
    }
}
