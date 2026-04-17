package com.dadadrive.ui.map

import android.util.Log
import com.here.sdk.core.GeoPolyline
import com.here.sdk.routing.Route

internal object MapTrafficUtils {
    private const val MAX_TRAFFIC_EXTRACTION_ROUTE_KM = 150.0
    private const val MIN_RELEVANT_JAM_FACTOR = 3.0

    fun extractTrafficSpans(route: Route, logTag: String): List<PassengerTrafficSpan> {
        // Skip heavy traffic extraction for very long routes (urban / intercity cap).
        if ((route.lengthInMeters / 1000.0) > MAX_TRAFFIC_EXTRACTION_ROUTE_KM) return emptyList()

        val spans = ArrayList<PassengerTrafficSpan>()
        for (section in route.sections) {
            for (span in section.spans) {
                val jamFactorRaw = resolveJamFactor(span.dynamicSpeedInfo, span) ?: continue
                val jamFactor = normalizeJamFactor(jamFactorRaw) ?: continue
                if (jamFactor <= MIN_RELEVANT_JAM_FACTOR) continue
                val copied = copyGeometry(span.geometry, logTag) ?: continue
                spans.add(PassengerTrafficSpan(geometry = copied, jamFactor = jamFactor))
            }
        }
        return spans
    }

    fun copyGeometry(geometry: GeoPolyline, logTag: String): GeoPolyline? =
        try {
            val vertices = geometry.vertices
            if (vertices.isEmpty()) null else GeoPolyline(vertices)
        } catch (e: Exception) {
            Log.w(logTag, "copyGeometry: ${e.message}")
            null
        }

    /**
     * HERE SDKs can expose jam factor on different scales depending on platform/version.
     * - Some builds return 0..1
     * - Others return 0..10
     * We normalize to 0..10 for rendering consistency with Swift thresholds.
     */
    private fun normalizeJamFactor(raw: Double): Double? {
        if (!raw.isFinite() || raw < 0.0) return null
        return when {
            raw <= 1.0 -> raw * 10.0
            raw <= 10.0 -> raw
            raw <= 100.0 -> raw / 10.0
            else -> 10.0
        }.coerceIn(0.0, 10.0)
    }

    private fun resolveJamFactor(dynamicSpeedInfo: Any?, span: Any): Double? {
        // 1) Prefer official dynamicSpeedInfo jam factor (Swift equivalent).
        if (dynamicSpeedInfo != null) {
            runCatching {
                val m = dynamicSpeedInfo.javaClass.methods.firstOrNull {
                    it.name == "calculateJamFactor" && it.parameterCount == 0
                }
                val value = m?.invoke(dynamicSpeedInfo) as? Number
                if (value != null) return value.toDouble()
            }
            runCatching {
                val m = dynamicSpeedInfo.javaClass.methods.firstOrNull {
                    (it.name == "getJamFactor" || it.name == "jamFactor") && it.parameterCount == 0
                }
                val value = m?.invoke(dynamicSpeedInfo) as? Number
                if (value != null) return value.toDouble()
            }
        }

        // 2) Fallback: compute a pseudo jam factor from traffic/base speed on the span.
        val trafficSpeed = callNumberGetter(span, "getTrafficSpeedInMetersPerSecond")
            ?: callNumberGetter(span, "trafficSpeedInMetersPerSecond")
        val baseSpeed = callNumberGetter(span, "getBaseSpeedInMetersPerSecond")
            ?: callNumberGetter(span, "baseSpeedInMetersPerSecond")
            ?: callNumberGetter(span, "getSpeedLimitInMetersPerSecond")
            ?: callNumberGetter(span, "speedLimitInMetersPerSecond")
        if (trafficSpeed != null && baseSpeed != null && baseSpeed > 0.1) {
            val ratio = (trafficSpeed / baseSpeed).coerceIn(0.0, 1.0)
            return ((1.0 - ratio) * 10.0).coerceIn(0.0, 10.0)
        }
        return null
    }

    private fun callNumberGetter(target: Any, methodName: String): Double? =
        runCatching {
            val m = target.javaClass.methods.firstOrNull {
                it.name == methodName && it.parameterCount == 0
            } ?: return null
            (m.invoke(target) as? Number)?.toDouble()
        }.getOrNull()
}
