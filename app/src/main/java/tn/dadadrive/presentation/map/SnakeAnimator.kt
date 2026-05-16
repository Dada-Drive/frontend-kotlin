package tn.dadadrive.presentation.map

import android.os.SystemClock
import android.view.Choreographer
import com.here.sdk.core.Color as HereColor
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.RenderSize
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

internal class SnakeAnimator(
    private val mapView: MapView
) : Choreographer.FrameCallback {

    private var routeCoords: List<GeoCoordinates> = emptyList()
    private var segmentLengths: List<Double> = emptyList()
    private var routeTotalLengthM: Double = 0.0

    private var snakeStartMs: Long = 0L
    private var lastRenderedDistanceM: Double = -1.0
    private var displayedDistanceM: Double = -1.0
    private var running: Boolean = false

    private var trailPolyline: MapPolyline? = null
    private var headPolyline: MapPolyline? = null

    override fun doFrame(frameTimeNanos: Long) {
        if (!running || routeTotalLengthM <= 0.0) return

        val nowMs = SystemClock.elapsedRealtimeNanos() / 1_000_000L
        val elapsedMs = nowMs - snakeStartMs
        val speedMetersPerMs = routeTotalLengthM / (SNAKE_DURATION_SEC * 1000.0)
        val traveledM = elapsedMs * speedMetersPerMs
        val targetProgressM = traveledM % routeTotalLengthM
        if (displayedDistanceM < 0.0) {
            displayedDistanceM = targetProgressM
        } else {
            val forwardDelta = wrapForwardDistance(displayedDistanceM, targetProgressM, routeTotalLengthM)
            displayedDistanceM += forwardDelta * SNAKE_PROGRESS_SMOOTHING
            if (displayedDistanceM >= routeTotalLengthM) displayedDistanceM -= routeTotalLengthM
        }
        val progressM = displayedDistanceM

        if (lastRenderedDistanceM >= 0.0 && abs(progressM - lastRenderedDistanceM) < SNAKE_MIN_STEP_M) {
            Choreographer.getInstance().postFrameCallback(this)
            return
        }

        render(progressM)
        lastRenderedDistanceM = progressM
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun setupSnakeAnimation(coords: List<GeoCoordinates>) {
        clearPolylines()
        routeCoords = coords
        if (routeCoords.size < 2) {
            segmentLengths = emptyList()
            routeTotalLengthM = 0.0
            lastRenderedDistanceM = -1.0
            return
        }
        val (lengths, total) = segmentLengths(routeCoords)
        segmentLengths = lengths
        routeTotalLengthM = total
        lastRenderedDistanceM = -1.0
        displayedDistanceM = -1.0
        snakeStartMs = SystemClock.elapsedRealtimeNanos() / 1_000_000L
        if (!running) {
            running = true
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun stopSnakeAnimation() {
        if (running) {
            Choreographer.getInstance().removeFrameCallback(this)
        }
        running = false
        routeCoords = emptyList()
        segmentLengths = emptyList()
        routeTotalLengthM = 0.0
        snakeStartMs = 0L
        lastRenderedDistanceM = -1.0
        displayedDistanceM = -1.0
        clearPolylines()
    }

    private fun render(progressM: Double) {
        clearPolylines()

        val trailCoords = slice(
            coords = routeCoords,
            lengths = segmentLengths,
            from = 0.0,
            to = progressM
        )
        trailPolyline = makeSnakePolyline(trailCoords, TRAIL_COLOR, SNAKE_TRAIL_WIDTH)?.also {
            it.drawOrder = 5
            mapView.mapScene.addMapPolyline(it)
        }

        val headStart = maxOf(0.0, progressM - SNAKE_HEAD_LENGTH_M)
        val headCoords = slice(
            coords = routeCoords,
            lengths = segmentLengths,
            from = headStart,
            to = progressM
        )
        headPolyline = makeSnakePolyline(headCoords, HEAD_COLOR, SNAKE_HEAD_WIDTH)?.also {
            it.drawOrder = 6
            mapView.mapScene.addMapPolyline(it)
        }
    }

    private fun clearPolylines() {
        trailPolyline?.let { mapView.mapScene.removeMapPolyline(it) }
        headPolyline?.let { mapView.mapScene.removeMapPolyline(it) }
        trailPolyline = null
        headPolyline = null
    }

    private fun makeSnakePolyline(
        coords: List<GeoCoordinates>,
        color: HereColor,
        widthPx: Float
    ): MapPolyline? {
        if (coords.size < 2) return null
        val geoPolyline = runCatching { GeoPolyline(coords) }.getOrNull() ?: return null
        val width = MapMeasureDependentRenderSize(
            RenderSize.Unit.PIXELS,
            widthPx.toDouble()
        )
        val representation = MapPolyline.SolidRepresentation(width, color, LineCap.ROUND)
        return runCatching { MapPolyline(geoPolyline, representation) }.getOrNull()
    }

    private fun haversineM(a: GeoCoordinates, b: GeoCoordinates): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lon1 = Math.toRadians(a.longitude)
        val lat2 = Math.toRadians(b.latitude)
        val lon2 = Math.toRadians(b.longitude)
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        val sinLat = sin(dLat / 2.0)
        val sinLon = sin(dLon / 2.0)
        val h = sinLat * sinLat + cos(lat1) * cos(lat2) * sinLon * sinLon
        val c = 2.0 * atan2(sqrt(h), sqrt(1.0 - h))
        return EARTH_RADIUS_M * c
    }

    private fun segmentLengths(coords: List<GeoCoordinates>): Pair<List<Double>, Double> {
        if (coords.size < 2) return emptyList<Double>() to 0.0
        val lengths = ArrayList<Double>(coords.size - 1)
        var total = 0.0
        for (i in 0 until coords.lastIndex) {
            val len = haversineM(coords[i], coords[i + 1])
            lengths.add(len)
            total += len
        }
        return lengths to total
    }

    private fun interpolate(
        coords: List<GeoCoordinates>,
        lengths: List<Double>,
        distance: Double
    ): GeoCoordinates {
        if (coords.isEmpty()) return GeoCoordinates(0.0, 0.0)
        if (coords.size == 1 || lengths.isEmpty()) return coords.first()
        val total = lengths.sum()
        if (distance <= 0.0) return coords.first()
        if (distance >= total) return coords.last()

        var remaining = distance
        for (i in lengths.indices) {
            val segment = lengths[i]
            if (segment <= 0.0) continue
            if (remaining <= segment) {
                val t = (remaining / segment).coerceIn(0.0, 1.0)
                val start = coords[i]
                val end = coords[i + 1]
                val lat = start.latitude + (end.latitude - start.latitude) * t
                val lon = start.longitude + (end.longitude - start.longitude) * t
                return GeoCoordinates(lat, lon)
            }
            remaining -= segment
        }
        return coords.last()
    }

    private fun slice(
        coords: List<GeoCoordinates>,
        lengths: List<Double>,
        from: Double,
        to: Double
    ): List<GeoCoordinates> {
        if (coords.size < 2 || lengths.isEmpty()) return emptyList()
        val total = lengths.sum()
        if (total <= 0.0) return emptyList()
        val start = min(from, to).coerceIn(0.0, total)
        val end = maxOf(from, to).coerceIn(0.0, total)
        if (end <= start) return emptyList()

        val out = ArrayList<GeoCoordinates>()
        out.add(interpolate(coords, lengths, start))

        var traveled = 0.0
        for (i in lengths.indices) {
            traveled += lengths[i]
            if (traveled > start && traveled < end) {
                out.add(coords[i + 1])
            }
        }

        val endPoint = interpolate(coords, lengths, end)
        if (!sameCoordinate(out.last(), endPoint)) {
            out.add(endPoint)
        }
        return out
    }

    private fun sameCoordinate(a: GeoCoordinates, b: GeoCoordinates): Boolean {
        return abs(a.latitude - b.latitude) < 1e-9 && abs(a.longitude - b.longitude) < 1e-9
    }

    private fun wrapForwardDistance(from: Double, to: Double, total: Double): Double {
        return if (to >= from) to - from else (total - from) + to
    }

    private companion object {
        const val SNAKE_DURATION_SEC = 2.9
        const val SNAKE_HEAD_LENGTH_M = 320.0
        const val SNAKE_MIN_STEP_M = 2.0
        const val SNAKE_TRAIL_WIDTH = 9f
        const val SNAKE_HEAD_WIDTH = 14f
        const val EARTH_RADIUS_M = 6_371_000.0
        const val SNAKE_PROGRESS_SMOOTHING = 0.32

        val TRAIL_COLOR: HereColor = HereColor.valueOf(41f / 255f, 199f / 255f, 1f, 0.98f)
        val HEAD_COLOR: HereColor = HereColor.valueOf(184f / 255f, 239f / 255f, 1f, 1.0f)
    }
}
