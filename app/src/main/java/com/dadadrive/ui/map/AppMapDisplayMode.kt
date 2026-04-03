package com.dadadrive.ui.map

import com.here.sdk.mapview.MapScheme

/**
 * Modes d’affichage carte alignés sur Swift [AppMapScheme] + `toHEREScheme(colorScheme:)`.
 */
enum class AppMapDisplayMode {
    /** Carte vectorielle standard (jour / nuit selon le thème). */
    NORMAL,

    /** Imagerie satellite. */
    SATELLITE,

    /** Routes + fond satellite (jour / nuit). */
    HYBRID
}

fun AppMapDisplayMode.toHereMapScheme(isDark: Boolean): MapScheme =
    when (this) {
        AppMapDisplayMode.NORMAL ->
            if (isDark) MapScheme.NORMAL_NIGHT else MapScheme.NORMAL_DAY
        AppMapDisplayMode.SATELLITE -> MapScheme.SATELLITE
        AppMapDisplayMode.HYBRID ->
            if (isDark) MapScheme.HYBRID_NIGHT else MapScheme.HYBRID_DAY
    }
