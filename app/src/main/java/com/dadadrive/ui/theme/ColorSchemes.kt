package com.dadadrive.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// ── Contraste : texte/icônes sur la couleur primaire (ex. ambre → texte foncé) ─

internal fun contentOnPrimary(primary: Color): Color =
    if (primary.luminance() > 0.5f) Color(0xFF121212) else Color.White

// ── Thème sombre (UI noir / gris foncé) ─────────────────────────────────────

private fun buildDarkScheme(
    name: String,
    primary: Color,
    primaryDisabled: Color
): AppColorScheme {
    val onPrimary = contentOnPrimary(primary.copy(alpha = 1f))
    return AppColorScheme(
        name              = name,
        primary           = primary,
        onPrimary         = onPrimary,
        primaryDisabled   = primaryDisabled,
        secondary         = Color(0xFF9E9E9E),
        onSecondary       = Color.White,
        background        = Color(0xFF000000),
        onBackground      = Color.White,
        surface           = Color(0xFF111111),
        darkSurface       = Color(0xFF111111),
        lightSurface      = Color(0xFFF5F5F5),
        inputBackground   = Color(0xFF1A1A1A),
        darkInput         = Color(0xFF1A1A1A),
        lightInput        = Color(0xFFEEEEEE),
        inputUnderline    = Color(0xFF444444),
        textPrimary       = Color.White,
        textSecondary     = Color(0xFFAAAAAA),
        textHint          = Color(0xFF888888),
        textLabel         = Color(0xFF9E9E9E),
        errorRed          = Color(0xFFE53935),
        successGreen      = Color(0xFF4CAF50),
        warningOrange     = Color(0xFFFF9800),
        infoBlue          = Color(0xFF2196F3),
        divider           = Color(0xFF2A2A2A),
        border            = Color(0xFF444444),
        buttonBackground  = Color.White,
        buttonText        = Color.Black,
        buttonDisabledBackground = Color(0xFF444444),
        buttonDisabledText       = Color(0xFF888888),
        locationMarkerBlue       = Color(0xFF2196F3),
        locationMarkerBlueDark   = Color(0xFF1565C0),
        locationBlueLight        = Color(0xFFBBDEFB),
        locationCirclePrecision  = Color(0x3C2196F3),
        facebookBlue      = Color(0xFF1877F2),
        googleRed         = Color(0xFFEA4335),
        greyHint          = Color(0xFF888888),
        greyLabel         = Color(0xFF9E9E9E),
        dividerGrey       = Color(0xFF2A2A2A),
        errorContainer    = Color(0xFFFFEDED),
        onErrorContainer  = Color(0xFFB00020),
        surfaceElevated   = Color(0xFF1A1A1A),
        surfaceMuted      = Color(0xFF2A2A2A),
        surfaceOverlaySemi = Color(0xEE1A1A1A),
        textTertiary      = Color(0xFF555555),
        textDisabled      = Color(0xFFCCCCCC),
        textCaption       = Color(0xFF666666),
        dragHandle        = Color(0xFF444444),
        outlineLight      = Color(0xFFBBBBBB)
    )
}

// ── Thème clair (UI blanc / gris clair) ─────────────────────────────────────

private fun buildLightScheme(
    name: String,
    primary: Color,
    primaryDisabled: Color
): AppColorScheme {
    val onPrimary = contentOnPrimary(primary.copy(alpha = 1f))
    return AppColorScheme(
        name              = name,
        primary           = primary,
        onPrimary         = onPrimary,
        primaryDisabled   = primaryDisabled,
        secondary         = Color(0xFF757575),
        onSecondary       = Color.White,
        background        = Color(0xFFFFFFFF),
        onBackground      = Color(0xFF121212),
        surface           = Color(0xFFF5F5F5),
        darkSurface       = Color(0xFFF5F5F5),
        lightSurface      = Color(0xFFF5F5F5),
        inputBackground   = Color(0xFFF5F5F5),
        darkInput         = Color(0xFFEEEEEE),
        lightInput        = Color(0xFFEEEEEE),
        inputUnderline    = Color(0xFFBDBDBD),
        textPrimary       = Color(0xFF121212),
        textSecondary     = Color(0xFF616161),
        textHint          = Color(0xFF757575),
        textLabel         = Color(0xFF616161),
        errorRed          = Color(0xFFE53935),
        successGreen      = Color(0xFF43A047),
        warningOrange     = Color(0xFFFB8C00),
        infoBlue          = Color(0xFF1E88E5),
        divider           = Color(0xFFE0E0E0),
        border            = Color(0xFFBDBDBD),
        buttonBackground  = Color(0xFF121212),
        buttonText        = Color.White,
        buttonDisabledBackground = Color(0xFFE0E0E0),
        buttonDisabledText       = Color(0xFF9E9E9E),
        locationMarkerBlue       = Color(0xFF1976D2),
        locationMarkerBlueDark   = Color(0xFF0D47A1),
        locationBlueLight        = Color(0xFF90CAF9),
        locationCirclePrecision  = Color(0x3C1976D2),
        facebookBlue      = Color(0xFF1877F2),
        googleRed         = Color(0xFFEA4335),
        greyHint          = Color(0xFF757575),
        greyLabel         = Color(0xFF616161),
        dividerGrey       = Color(0xFFE0E0E0),
        errorContainer    = Color(0xFFFFEDED),
        onErrorContainer  = Color(0xFFB00020),
        surfaceElevated   = Color(0xFFFFFFFF),
        surfaceMuted      = Color(0xFFEEEEEE),
        surfaceOverlaySemi = Color(0xEEF5F5F5),
        textTertiary      = Color(0xFF757575),
        textDisabled      = Color(0xFFBDBDBD),
        textCaption       = Color(0xFF616161),
        dragHandle        = Color(0xFFBDBDBD),
        outlineLight      = Color(0xFFBBBBBB)
    )
}

// ── Enum : seules les couleurs de marque sont stockées ──────────────────────

enum class AppTheme(
    val displayName: String,
    val brandPrimary: Color,
    val brandPrimaryDisabled: Color
) {
    GREEN("Vert (Défaut)",  Color(0xFF80C000), Color(0xFFA8D860)),
    BLUE("Bleu",            Color(0xFF2196F3), Color(0xFF90CAF9)),
    PURPLE("Violet",        Color(0xFF9C27B0), Color(0xFFCE93D8)),
    ORANGE("Orange",        Color(0xFFFF6D00), Color(0xFFFFAB40)),
    PINK("Rose",             Color(0xFFE91E63), Color(0xFFF48FB1)),
    TEAL("Turquoise",       Color(0xFF009688), Color(0xFF80CBC4)),
    RED("Rouge",             Color(0xFFF44336), Color(0xFFEF9A9A)),
    AMBER("Ambre",          Color(0xFFFFC107), Color(0xFFFFE082));

    /** Schéma complet selon le mode système (clair / sombre). */
    fun resolveScheme(isDark: Boolean): AppColorScheme =
        if (isDark) {
            buildDarkScheme(displayName, brandPrimary, brandPrimaryDisabled)
        } else {
            buildLightScheme(displayName, brandPrimary, brandPrimaryDisabled)
        }

    companion object {
        fun fromName(name: String): AppTheme =
            entries.firstOrNull { it.name == name } ?: GREEN
    }
}

/** Schéma sombre par défaut (logo splash, previews). */
val GreenSchemeDark: AppColorScheme =
    AppTheme.GREEN.resolveScheme(isDark = true)

/** Toutes les entrées couleur du schéma (aperçu réglages / debug). */
fun AppColorScheme.allColorEntries(): List<Pair<String, Color>> = listOf(
    "primary" to primary,
    "onPrimary" to onPrimary,
    "primaryDisabled" to primaryDisabled,
    "secondary" to secondary,
    "onSecondary" to onSecondary,
    "background" to background,
    "onBackground" to onBackground,
    "surface" to surface,
    "darkSurface" to darkSurface,
    "lightSurface" to lightSurface,
    "inputBackground" to inputBackground,
    "darkInput" to darkInput,
    "lightInput" to lightInput,
    "inputUnderline" to inputUnderline,
    "textPrimary" to textPrimary,
    "textSecondary" to textSecondary,
    "textHint" to textHint,
    "textLabel" to textLabel,
    "errorRed" to errorRed,
    "successGreen" to successGreen,
    "warningOrange" to warningOrange,
    "infoBlue" to infoBlue,
    "divider" to divider,
    "border" to border,
    "buttonBackground" to buttonBackground,
    "buttonText" to buttonText,
    "buttonDisabledBackground" to buttonDisabledBackground,
    "buttonDisabledText" to buttonDisabledText,
    "locationMarkerBlue" to locationMarkerBlue,
    "locationMarkerBlueDark" to locationMarkerBlueDark,
    "locationBlueLight" to locationBlueLight,
    "locationCirclePrecision" to locationCirclePrecision,
    "facebookBlue" to facebookBlue,
    "googleRed" to googleRed,
    "greyHint" to greyHint,
    "greyLabel" to greyLabel,
    "dividerGrey" to dividerGrey,
    "errorContainer" to errorContainer,
    "onErrorContainer" to onErrorContainer,
    "surfaceElevated" to surfaceElevated,
    "surfaceMuted" to surfaceMuted,
    "surfaceOverlaySemi" to surfaceOverlaySemi,
    "textTertiary" to textTertiary,
    "textDisabled" to textDisabled,
    "textCaption" to textCaption,
    "dragHandle" to dragHandle,
    "outlineLight" to outlineLight
)
