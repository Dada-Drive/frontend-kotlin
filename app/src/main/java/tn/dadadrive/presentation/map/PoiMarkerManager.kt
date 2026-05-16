package tn.dadadrive.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.here.sdk.core.Anchor2D
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.gestures.TapListener
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapView
import com.here.sdk.search.Place
import kotlin.math.roundToInt

internal class PoiMarkerManager(
    private val context: Context,
    private val mapView: MapView
) {
    private val poiMarkers = mutableListOf<MapMarker>()
    private val markerPlaces = LinkedHashMap<MapMarker, Place>()
    private var lastPlaces: List<Place> = emptyList()
    private var selectedCategory: PoiCategory? = null
    private val poiBitmap: Bitmap by lazy {
        val raw = context.assets.open("home.png").use { stream ->
            BitmapFactory.decodeStream(stream)
        }
        val targetPx = (context.resources.displayMetrics.density * 22f).roundToInt().coerceAtLeast(16)
        Bitmap.createScaledBitmap(raw, targetPx, targetPx, true)
    }

    var onPoiMarkerTapped: ((Place, PoiCategory) -> Unit)? = null

    init {
        mapView.gestures.tapListener = TapListener { touchPoint ->
            val tappedGeo = mapView.viewToGeoCoordinates(touchPoint) ?: return@TapListener
            val category = selectedCategory ?: return@TapListener
            val tappedPlace = nearestPlace(tappedGeo, lastPlaces) ?: return@TapListener
            onPoiMarkerTapped?.invoke(tappedPlace, category)
        }
    }

    fun showPoiResults(places: List<Place>, category: PoiCategory) {
        clearPoiMarkers()
        selectedCategory = category
        lastPlaces = places
        val image = MapImageFactory.fromBitmap(poiBitmap)
        places.take(60).forEach { place ->
            val geo = place.geoCoordinates ?: return@forEach
            val marker = MapMarker(geo, image, Anchor2D(0.5, 1.0))
            mapView.mapScene.addMapMarker(marker)
            poiMarkers.add(marker)
            markerPlaces[marker] = place
        }
    }

    fun clearPoiMarkers() {
        poiMarkers.forEach { mapView.mapScene.removeMapMarker(it) }
        poiMarkers.clear()
        markerPlaces.clear()
        lastPlaces = emptyList()
        selectedCategory = null
    }

    private fun nearestPlace(tap: GeoCoordinates, places: List<Place>): Place? {
        var best: Place? = null
        var bestDistance = Double.MAX_VALUE
        places.forEach { place ->
            val geo = place.geoCoordinates ?: return@forEach
            val dist = tap.distanceTo(geo)
            if (dist < bestDistance) {
                bestDistance = dist
                best = place
            }
        }
        return if (bestDistance <= 180.0) best else null
    }
}
