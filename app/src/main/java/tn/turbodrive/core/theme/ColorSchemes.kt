package tn.turbodrive.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// ── Contraste : texte/icônes sur la couleur primaire (ex. ambre → texte foncé) ─

internal fun contentOnPrimary(primary: Color): Color = if (primary.luminance() > 0.5f) Color(0xFF121212) else Color.White

// ── Thème sombre (UI noir / gris foncé) ─────────────────────────────────────

@Suppress("LongMethod", "MagicNumber")
private fun buildDarkScheme(
    name: String,
    primary: Color,
    primaryDisabled: Color,
): AppColorScheme {
    val onPrimary = contentOnPrimary(primary.copy(alpha = 1f))
    return AppColorScheme(
        name = name,
        primary = primary,
        onPrimary = onPrimary,
        primaryDisabled = primaryDisabled,
        secondary = Color(0xFF9E9E9E),
        onSecondary = Color.White,
        background = Color(0xFF111111),
        onBackground = Color.White,
        surface = Color(0xFF1A1A1A),
        darkSurface = Color(0xFF1A1A1A),
        lightSurface = Color(0xFFF5F5F5),
        inputBackground = Color(0xFF1A1A1A),
        darkInput = Color(0xFF1A1A1A),
        lightInput = Color(0xFFEEEEEE),
        inputUnderline = Color(0xFF444444),
        textPrimary = Color.White,
        textSecondary = Color(0xFFAAAAAA),
        textSubtle = Color(0xFF888888),
        textLabel = Color(0xFF9E9E9E),
        error = Color(0xFFE53935),
        accent = Color(0xFF4CAF50),
        warning = Color(0xFFFF9800),
        info = Color(0xFF2196F3),
        divider = Color(0xFF2A2A2A),
        border = Color(0xFF444444),
        buttonBackground = Color.White,
        buttonText = Color.Black,
        buttonDisabledBackground = Color(0xFF444444),
        buttonDisabledText = Color(0xFF888888),
        locationMarkerBlue = Color(0xFF2196F3),
        locationMarkerBlueDark = Color(0xFF1565C0),
        locationBlueLight = Color(0xFFBBDEFB),
        locationCirclePrecision = Color(0x3C2196F3),
        facebookBlue = Color(0xFF1877F2),
        googleRed = Color(0xFFEA4335),
        greyHint = Color(0xFF888888),
        greyLabel = Color(0xFF9E9E9E),
        dividerGrey = Color(0xFF2A2A2A),
        errorSoft = Color(0xFFFFEDED),
        onErrorContainer = Color(0xFFB00020),
        accentSoft = Color(0xFF14532D),
        surfaceElevated = Color(0xFF1A1A1A),
        surfaceAlt = Color(0xFF2A2A2A),
        surfaceOverlaySemi = Color(0xEE1A1A1A),
        textTertiary = Color(0xFF555555),
        textDisabled = Color(0xFFCCCCCC),
        textCaption = Color(0xFF666666),
        dragHandle = Color(0xFF444444),
        borderStrong = Color(0xFFBBBBBB),
        ratingYellow = Color(0xFFFFC107),
        coinSilver = Color(0xFF9B9B9B),
        coinGold = Color(0xFFB89530),
        // R-4.5 v2 tokens — fallback dark values for alternate themes
        accentInk = Color(0xFF4ADE80),
        surfaceDeep = Color(0xFF28282D),
        inkSoft = Color(0xFFE5E5E0),
        inkSubtle = Color(0xFFB8B8B0),
        warningSoft = Color(0xFF3A2A0A),
        infoSoft = Color(0xFF0F1F3A),
        mapLand = Color(0xFF1A1F2E),
        mapWater = Color(0xFF0E1320),
        mapRoad = Color(0xFF2A3142),
        mapPath = Color(0xFF3A4358),
    )
}

// ── Thème clair (UI blanc / gris clair) ─────────────────────────────────────

@Suppress("LongMethod", "MagicNumber")
private fun buildLightScheme(
    name: String,
    primary: Color,
    primaryDisabled: Color,
): AppColorScheme {
    val onPrimary = contentOnPrimary(primary.copy(alpha = 1f))
    return AppColorScheme(
        name = name,
        primary = primary,
        onPrimary = onPrimary,
        primaryDisabled = primaryDisabled,
        secondary = Color(0xFF757575),
        onSecondary = Color.White,
        background = Color(0xFFF5F5F5),
        onBackground = Color(0xFF111111),
        surface = Color(0xFFFFFFFF),
        darkSurface = Color(0xFFF5F5F5),
        lightSurface = Color(0xFFF5F5F5),
        inputBackground = Color(0xFFF5F5F5),
        darkInput = Color(0xFFEEEEEE),
        lightInput = Color(0xFFEEEEEE),
        inputUnderline = Color(0xFFBDBDBD),
        textPrimary = Color(0xFF111111),
        textSecondary = Color(0xFF616161),
        textSubtle = Color(0xFF757575),
        textLabel = Color(0xFF616161),
        error = Color(0xFFE53935),
        accent = Color(0xFF43A047),
        warning = Color(0xFFFB8C00),
        info = Color(0xFF1E88E5),
        divider = Color(0xFFE0E0E0),
        border = Color(0xFFBDBDBD),
        buttonBackground = Color(0xFF121212),
        buttonText = Color.White,
        buttonDisabledBackground = Color(0xFFE0E0E0),
        buttonDisabledText = Color(0xFF9E9E9E),
        locationMarkerBlue = Color(0xFF1976D2),
        locationMarkerBlueDark = Color(0xFF0D47A1),
        locationBlueLight = Color(0xFF90CAF9),
        locationCirclePrecision = Color(0x3C1976D2),
        facebookBlue = Color(0xFF1877F2),
        googleRed = Color(0xFFEA4335),
        greyHint = Color(0xFF757575),
        greyLabel = Color(0xFF616161),
        dividerGrey = Color(0xFFE0E0E0),
        errorSoft = Color(0xFFFFEDED),
        onErrorContainer = Color(0xFFB00020),
        accentSoft = Color(0xFFDCFCE7),
        surfaceElevated = Color(0xFFFFFFFF),
        surfaceAlt = Color(0xFFEEEEEE),
        surfaceOverlaySemi = Color(0xEEF5F5F5),
        textTertiary = Color(0xFF757575),
        textDisabled = Color(0xFFBDBDBD),
        textCaption = Color(0xFF616161),
        dragHandle = Color(0xFFBDBDBD),
        borderStrong = Color(0xFFBBBBBB),
        ratingYellow = Color(0xFFFFC107),
        coinSilver = Color(0xFFC0C0C0),
        coinGold = Color(0xFFD4AF37),
        // R-4.5 v2 tokens — fallback light values for alternate themes
        accentInk = Color(0xFF15803D),
        surfaceDeep = Color(0xFFEAE7E0),
        inkSoft = Color(0xFF1F1F1F),
        inkSubtle = Color(0xFF3D3D3D),
        warningSoft = Color(0xFFFEF3C7),
        infoSoft = Color(0xFFDBEAFE),
        mapLand = Color(0xFFEEEBE0),
        mapWater = Color(0xFFD8DCE4),
        mapRoad = Color(0xFFFFFFFF),
        mapPath = Color(0xFFDDD7C5),
    )
}

// ── Enum : seules les couleurs de marque sont stockées ──────────────────────

enum class AppTheme(
    val displayName: String,
    val brandPrimary: Color,
    val brandPrimaryDisabled: Color,
) {
    TURBODRIVE("TurboDrive", Color(0xFF0A0A0A), Color(0xFFEAE7E0)),
    GREEN("Vert", Color(0xFF80C000), Color(0xFFA8D860)),
    BLUE("Bleu", Color(0xFF2196F3), Color(0xFF90CAF9)),
    PURPLE("Violet", Color(0xFF9C27B0), Color(0xFFCE93D8)),
    ORANGE("Orange", Color(0xFFFF6D00), Color(0xFFFFAB40)),
    PINK("Rose", Color(0xFFE91E63), Color(0xFFF48FB1)),
    TEAL("Turquoise", Color(0xFF009688), Color(0xFF80CBC4)),
    RED("Rouge", Color(0xFFF44336), Color(0xFFEF9A9A)),
    AMBER("Ambre", Color(0xFFFFC107), Color(0xFFFFE082)),
    ;

    /**
     * Schéma complet selon le mode système (clair / sombre).
     * @param secondaryOverride si non null, remplace la couleur secondaire Material (boutons secondaires, etc.).
     */
    fun resolveScheme(
        isDark: Boolean,
        secondaryOverride: Color? = null,
    ): AppColorScheme {
        val base =
            when (this) {
                TURBODRIVE -> buildTurboDriveColorScheme(isDark)
                else ->
                    if (isDark) {
                        buildDarkScheme(displayName, brandPrimary, brandPrimaryDisabled)
                    } else {
                        buildLightScheme(displayName, brandPrimary, brandPrimaryDisabled)
                    }
            }
        if (secondaryOverride == null) return base
        val s = secondaryOverride.copy(alpha = 1f)
        return base.copy(
            secondary = s,
            onSecondary = contentOnPrimary(s),
        )
    }

    companion object {
        fun fromName(name: String): AppTheme = entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: TURBODRIVE
    }
}

/** Schémas redesign (défaut app). */
val TurboDriveSchemeLight: AppColorScheme = AppTheme.TURBODRIVE.resolveScheme(isDark = false)
val TurboDriveSchemeDark: AppColorScheme = AppTheme.TURBODRIVE.resolveScheme(isDark = true)

/** Ancien schéma sombre vert (thème classique). */
val GreenSchemeDark: AppColorScheme =
    AppTheme.GREEN.resolveScheme(isDark = true)

/** Toutes les entrées couleur du schéma (aperçu réglages / debug). */
@Suppress("LongMethod")
fun AppColorScheme.allColorEntries(): List<Pair<String, Color>> =
    listOf(
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
        "textSubtle" to textSubtle,
        "textLabel" to textLabel,
        "error" to error,
        "accent" to accent,
        "warning" to warning,
        "info" to info,
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
        "errorSoft" to errorSoft,
        "onErrorContainer" to onErrorContainer,
        "accentSoft" to accentSoft,
        "surfaceElevated" to surfaceElevated,
        "surfaceAlt" to surfaceAlt,
        "surfaceOverlaySemi" to surfaceOverlaySemi,
        "textTertiary" to textTertiary,
        "textDisabled" to textDisabled,
        "textCaption" to textCaption,
        "dragHandle" to dragHandle,
        "borderStrong" to borderStrong,
        "ratingYellow" to ratingYellow,
        "coinSilver" to coinSilver,
        "coinGold" to coinGold,
        "accentInk" to accentInk,
        "surfaceDeep" to surfaceDeep,
        "inkSoft" to inkSoft,
        "inkSubtle" to inkSubtle,
        "warningSoft" to warningSoft,
        "infoSoft" to infoSoft,
        "mapLand" to mapLand,
        "mapWater" to mapWater,
        "mapRoad" to mapRoad,
        "mapPath" to mapPath,
    )
