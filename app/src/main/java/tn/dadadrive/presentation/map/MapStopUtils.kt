package tn.dadadrive.presentation.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Google Maps yellow, matches the other colored teardrop pins on the map. */
internal const val STOP_PIN_YELLOW_ARGB: Int = 0xFFFFC107.toInt()

/** Simple marker descriptor consumed by [HereMapViewComposable]. */
data class IntermediateStopMarker(
    val coordinates: GeoCoordinates,
    val isValid: Boolean
)

enum class IntermediateStopValidity {
    /** Point is allowed and displayed as a normal yellow stop marker. */
    OnRoute,

    /** Kept for backward compatibility with existing UI state handling. */
    OffRoute,

    /** We don't have a route or coordinates yet; validity is unknown. */
    Unknown
}

/**
 * A draft intermediate stop entered by the rider before the ride is created.
 * The text label may be present before we have coordinates (user is typing).
 */
data class IntermediateStopDraft(
    val label: String,
    val coordinates: GeoCoordinates?,
    val validity: IntermediateStopValidity
) {
    fun isReady(): Boolean =
        coordinates != null && label.isNotBlank() && validity == IntermediateStopValidity.OnRoute
}

/**
 * Computes the minimum distance in meters from [point] to the union of [polylines]
 * (a single route is a list of polylines for alternates / legs). Returns
 * [Double.POSITIVE_INFINITY] when there's no polyline vertex available.
 *
 * We project each polyline segment into a local ENU tangent plane centered on the
 * midpoint, so perpendicular distance is exact enough for short segments (≲ few km).
 */
internal fun minDistanceMetersToPolylines(
    point: GeoCoordinates,
    polylines: List<GeoPolyline>
): Double {
    if (polylines.isEmpty()) return Double.POSITIVE_INFINITY
    var best = Double.POSITIVE_INFINITY
    for (poly in polylines) {
        val verts = poly.vertices
        if (verts.size < 2) {
            if (verts.isNotEmpty()) {
                best = min(best, haversineMeters(point, verts[0]))
            }
            continue
        }
        for (i in 0 until verts.size - 1) {
            val d = distanceToSegmentMeters(point, verts[i], verts[i + 1])
            if (d < best) best = d
            if (best == 0.0) return 0.0
        }
    }
    return best
}

private fun haversineMeters(a: GeoCoordinates, b: GeoCoordinates): Double {
    val r = 6_371_000.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val h = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(sqrt(h), sqrt(1 - h))
    return r * c
}

/**
 * Distance from [p] to segment [a]-[b] in meters using an equirectangular
 * approximation around the segment midpoint (good for segments ≤ ~5 km).
 */
private fun distanceToSegmentMeters(
    p: GeoCoordinates,
    a: GeoCoordinates,
    b: GeoCoordinates
): Double {
    val midLatRad = Math.toRadians((a.latitude + b.latitude) / 2.0)
    val metersPerDegLat = 111_320.0
    val metersPerDegLon = metersPerDegLat * cos(midLatRad)

    val ax = (a.longitude - p.longitude) * metersPerDegLon
    val ay = (a.latitude - p.latitude) * metersPerDegLat
    val bx = (b.longitude - p.longitude) * metersPerDegLon
    val by = (b.latitude - p.latitude) * metersPerDegLat

    val dx = bx - ax
    val dy = by - ay
    val segLenSq = dx * dx + dy * dy
    if (segLenSq == 0.0) return sqrt(ax * ax + ay * ay)

    val t = max(0.0, min(1.0, -(ax * dx + ay * dy) / segLenSq))
    val projX = ax + t * dx
    val projY = ay + t * dy
    return sqrt(projX * projX + projY * projY)
}

/**
 * Rider can choose stop location freely. Any resolved point is valid.
 */
internal fun evaluateStopValidity(
    point: GeoCoordinates?,
    polylines: List<GeoPolyline>
): IntermediateStopValidity {
    return if (point == null) IntermediateStopValidity.Unknown else IntermediateStopValidity.OnRoute
}

/**
 * Draws a push pin (same style as [createPushPinBitmap]) with a red forbidden-circle
 * overlay (circle with diagonal slash) on top of the head — used to indicate a stop
 * that is too far from the route.
 */
internal fun createForbiddenPushPinBitmap(colorArgb: Int): Bitmap {
    val base = createPushPinBitmap(colorArgb)
    val bmp = base.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(bmp)
    val scale = 2.1f
    val ballRadius = 12f * scale
    val centerX = bmp.width / 2f
    val centerY = ballRadius

    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFFD32F2F.toInt()
        strokeWidth = 3.4f * scale / 1.1f
        strokeCap = Paint.Cap.ROUND
    }
    val radius = ballRadius * 0.95f
    canvas.drawCircle(centerX, centerY, radius, ring)

    val slash = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFFD32F2F.toInt()
        strokeWidth = 3.4f * scale / 1.1f
        strokeCap = Paint.Cap.ROUND
    }
    val offset = radius * 0.72f
    canvas.drawLine(
        centerX - offset,
        centerY - offset,
        centerX + offset,
        centerY + offset,
        slash
    )
    return bmp
}
