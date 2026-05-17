package tn.turbodrive.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Couleurs [design-system.md] §2.1 (clair) et §2.2 (sombre) — redesign TurboDrive v2.
 * Les champs [AppColorScheme] réutilisent les noms historiques du projet (mapping sémantique).
 */
internal fun buildTurboDriveColorScheme(isDark: Boolean): AppColorScheme = if (isDark) turboDriveDarkScheme() else turboDriveLightScheme()

private fun turboDriveLightScheme(): AppColorScheme {
    val primary = Color(0xFF0A0A0A)
    val onPrimary = Color(0xFFFFFFFF)
    val surfaceDeep = Color(0xFFEAE7E0)
    return AppColorScheme(
        name = "TurboDrive (clair)",
        primary = primary,
        onPrimary = onPrimary,
        primaryDisabled = surfaceDeep,
        secondary = Color(0xFF5C5C5C),
        onSecondary = Color(0xFFFFFFFF),
        background = Color(0xFFF6F5F1),
        onBackground = Color(0xFF0A0A0A),
        surface = Color(0xFFFFFFFF),
        darkSurface = Color(0xFFF1EFEA),
        lightSurface = Color(0xFFFFFFFF),
        inputBackground = Color(0xFFFFFFFF),
        darkInput = Color(0xFFF1EFEA),
        lightInput = Color(0xFFFFFFFF),
        inputUnderline = Color(0xFFE5E2D8),
        textPrimary = Color(0xFF0A0A0A),
        textSecondary = Color(0xFF5C5C5C),
        textHint = Color(0xFF8A8A85),
        textLabel = Color(0xFF5C5C5C),
        errorRed = Color(0xFFDC2626),
        successGreen = Color(0xFF16A34A),
        warningOrange = Color(0xFFD97706),
        infoBlue = Color(0xFF2563EB),
        divider = Color(0xFFEDEAE0),
        border = Color(0xFFE5E2D8),
        buttonBackground = primary,
        buttonText = onPrimary,
        buttonDisabledBackground = surfaceDeep,
        buttonDisabledText = Color(0xFFB5B3AC),
        locationMarkerBlue = Color(0xFF2563EB),
        locationMarkerBlueDark = Color(0xFF1D4ED8),
        locationBlueLight = Color(0xFFDBEAFE),
        locationCirclePrecision = Color(0x402563EB),
        facebookBlue = Color(0xFF1877F2),
        googleRed = Color(0xFFEA4335),
        greyHint = Color(0xFF8A8A85),
        greyLabel = Color(0xFF5C5C5C),
        dividerGrey = Color(0xFFEDEAE0),
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFFDC2626),
        successContainer = Color(0xFFDCFCE7),
        surfaceElevated = Color(0xFFFFFFFF),
        surfaceMuted = Color(0xFFF1EFEA),
        surfaceOverlaySemi = Color(0x80000000),
        textTertiary = Color(0xFF5C5C5C),
        textDisabled = Color(0xFFB5B3AC),
        textCaption = Color(0xFF8A8A85),
        dragHandle = Color(0xFF8A8A85),
        outlineLight = Color(0xFFCFCABE),
        ratingYellow = Color(0xFFFFC107),
        coinSilver = Color(0xFFC0C0C0),
        coinGold = Color(0xFFD4AF37),
    )
}

private fun turboDriveDarkScheme(): AppColorScheme {
    val primary = Color(0xFFF4F4F0)
    val onPrimary = Color(0xFF0A0A0A)
    val surfaceDeep = Color(0xFF28282D)
    return AppColorScheme(
        name = "TurboDrive (sombre)",
        primary = primary,
        onPrimary = onPrimary,
        primaryDisabled = surfaceDeep,
        secondary = Color(0xFFA8A8A2),
        onSecondary = Color(0xFF0A0A0A),
        background = Color(0xFF0A0A0C),
        onBackground = Color(0xFFF4F4F0),
        surface = Color(0xFF161618),
        darkSurface = Color(0xFF1F1F22),
        lightSurface = Color(0xFF161618),
        inputBackground = Color(0xFF161618),
        darkInput = Color(0xFF1F1F22),
        lightInput = Color(0xFF28282D),
        inputUnderline = Color(0xFF3A3A40),
        textPrimary = Color(0xFFF4F4F0),
        textSecondary = Color(0xFFA8A8A2),
        textHint = Color(0xFF6E6E68),
        textLabel = Color(0xFFA8A8A2),
        errorRed = Color(0xFFEF4444),
        successGreen = Color(0xFF22C55E),
        warningOrange = Color(0xFFF59E0B),
        infoBlue = Color(0xFF3B82F6),
        divider = Color(0xFF22222A),
        border = Color(0xFF2A2A2E),
        buttonBackground = primary,
        buttonText = onPrimary,
        buttonDisabledBackground = surfaceDeep,
        buttonDisabledText = Color(0xFF454540),
        locationMarkerBlue = Color(0xFF3B82F6),
        locationMarkerBlueDark = Color(0xFF2563EB),
        locationBlueLight = Color(0xFF1E3A5F),
        locationCirclePrecision = Color(0x403B82F6),
        facebookBlue = Color(0xFF1877F2),
        googleRed = Color(0xFFEA4335),
        greyHint = Color(0xFF6E6E68),
        greyLabel = Color(0xFFA8A8A2),
        dividerGrey = Color(0xFF22222A),
        errorContainer = Color(0xFF3B0E13),
        onErrorContainer = Color(0xFFEF4444),
        successContainer = Color(0xFF14532D),
        surfaceElevated = Color(0xFF161618),
        surfaceMuted = Color(0xFF1F1F22),
        surfaceOverlaySemi = Color(0x80000000),
        textTertiary = Color(0xFFA8A8A2),
        textDisabled = Color(0xFF454540),
        textCaption = Color(0xFF6E6E68),
        dragHandle = Color(0xFF6E6E68),
        outlineLight = Color(0xFF3A3A40),
        ratingYellow = Color(0xFFFFC107),
        coinSilver = Color(0xFF9B9B9B),
        coinGold = Color(0xFFB89530),
    )
}
