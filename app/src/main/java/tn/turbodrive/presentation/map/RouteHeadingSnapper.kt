package tn.turbodrive.presentation.map

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Projects a GPS position onto the nearest route segment and returns the full
 * map-match: snapped coordinates + tangent bearing (deg, clockwise from true
 * north) + distance from the raw fix.
 *
 * Used to "stick" the driver taxi marker to the actual road direction AND
 * position instead of trusting noisy raw GPS at low speed / when stationary —
 * classic navigation map-matching (minus topological refinements).
 *
 * Returns null when:
 *   - no polylines supplied,
 *   - nearest segment is farther than [maxSnapMeters] from [position].
 *
 * Picks the polyline whose nearest segment has its local *tangent* closest to
 * [referenceHeadingDeg] when supplied (so we don't snap onto the opposite
 * direction of a two-way road with parallel geometries).
 */
internal object RouteHeadingSnapper {
    data class Match(
        val position: GeoCoordinates,
        val bearingDeg: Float,
        val distanceMeters: Double,
    )

    fun match(
        position: GeoCoordinates,
        polylines: List<GeoPolyline>,
        referenceHeadingDeg: Float? = null,
        maxSnapMeters: Double = 30.0,
        maxReferenceDivergenceDeg: Float = 90f,
    ): Match? {
        if (polylines.isEmpty()) return null

        var bestDistance = Double.MAX_VALUE
        var bestBearing: Double? = null
        var bestSnappedLat: Double = 0.0
        var bestSnappedLng: Double = 0.0

        for (poly in polylines) {
            val verts = poly.vertices
            if (verts.size < 2) continue
            for (i in 0 until verts.size - 1) {
                val a = verts[i]
                val b = verts[i + 1]
                if (a.latitude == b.latitude && a.longitude == b.longitude) continue

                val projection = projectOntoSegment(position, a, b)
                if (projection.distance >= bestDistance) continue

                val bearing = initialBearingDeg(a, b)
                if (referenceHeadingDeg != null) {
                    val diff = shortestAngleDiffDeg(bearing.toFloat(), referenceHeadingDeg)
                    if (kotlin.math.abs(diff) > maxReferenceDivergenceDeg) continue
                }

                bestDistance = projection.distance
                bestBearing = bearing
                bestSnappedLat = projection.lat
                bestSnappedLng = projection.lng
            }
        }

        val bearing = bestBearing ?: return null
        if (bestDistance > maxSnapMeters) return null
        return Match(
            position = GeoCoordinates(bestSnappedLat, bestSnappedLng),
            bearingDeg = normalizeDeg(bearing.toFloat()),
            distanceMeters = bestDistance,
        )
    }

    /** Returns only the bearing of the best map-match, or null if none within range. */
    fun snap(
        position: GeoCoordinates,
        polylines: List<GeoPolyline>,
        referenceHeadingDeg: Float? = null,
        maxSnapMeters: Double = 30.0,
        maxReferenceDivergenceDeg: Float = 90f,
    ): Float? =
        match(
            position = position,
            polylines = polylines,
            referenceHeadingDeg = referenceHeadingDeg,
            maxSnapMeters = maxSnapMeters,
            maxReferenceDivergenceDeg = maxReferenceDivergenceDeg,
        )?.bearingDeg

    private data class Projection(val lat: Double, val lng: Double, val distance: Double)

    private fun projectOntoSegment(
        p: GeoCoordinates,
        a: GeoCoordinates,
        b: GeoCoordinates,
    ): Projection {
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
        if (segLenSq == 0.0) {
            return Projection(a.latitude, a.longitude, hypot(ax, ay))
        }

        val t = (-(ax * dx + ay * dy) / segLenSq).coerceIn(0.0, 1.0)
        // Project in local metric space, then unwind back to geographic coordinates.
        val projX = ax + t * dx
        val projY = ay + t * dy
        val distance = hypot(projX, projY)
        val snappedLat = a.latitude + t * (b.latitude - a.latitude)
        val snappedLng = a.longitude + t * (b.longitude - a.longitude)
        return Projection(snappedLat, snappedLng, distance)
    }

    private fun initialBearingDeg(
        from: GeoCoordinates,
        to: GeoCoordinates,
    ): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    private fun normalizeDeg(deg: Float): Float {
        var d = deg % 360f
        if (d < 0f) d += 360f
        return d
    }

    private fun shortestAngleDiffDeg(
        a: Float,
        b: Float,
    ): Float {
        var d = (a - b) % 360f
        if (d > 180f) d -= 360f
        if (d < -180f) d += 360f
        return d
    }
}
