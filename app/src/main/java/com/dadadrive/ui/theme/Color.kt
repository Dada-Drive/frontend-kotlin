package com.dadadrive.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Backward-compatible constants (valeurs par défaut) ──────────────────────
val DadaDriveGreen         = Color(0xFF80C000)
val DadaDriveGreenDisabled = Color(0xFFA8D860)
val Black          = Color(0xFF000000)
val DarkSurface    = Color(0xFF111111)
val DarkInput      = Color(0xFF1A1A1A)
val White          = Color(0xFFFFFFFF)
val LightSurface   = Color(0xFFF5F5F5)
val LightInput     = Color(0xFFEEEEEE)
val GreyHint       = Color(0xFF888888)
val GreyLabel      = Color(0xFF9E9E9E)
val DividerGrey    = Color(0xFF2A2A2A)
val InputUnderline = Color(0xFF444444)
val ErrorRed       = Color(0xFFE53935)
val FacebookBlue   = Color(0xFF1877F2)
val GoogleRed      = Color(0xFFEA4335)

object AppColor {
    @get:Composable
    val background: Color
        get() = if (isSystemInDarkTheme()) Color(0xFF111111) else Color(0xFFF5F5F5)

    @get:Composable
    val surface: Color
        get() = if (isSystemInDarkTheme()) Color(0xFF1A1A1A) else Color.White

    @get:Composable
    val textPrimary: Color
        get() = if (isSystemInDarkTheme()) Color.White else Color(0xFF111111)

    val textHint = Color(0xFF888888)
    val textOnGreen = Color(0xFF111111)
    val green = Color(0xFF80C000)
    val greenDisabled = Color(0xFFA8D860)
    val error = Color(0xFFE53935)
    val destination: Color get() = error
}

fun Color.lighter(by: Float): Color = lerpColor(this, Color.White, by.coerceIn(0f, 1f))
fun Color.darker(by: Float): Color = lerpColor(this, Color.Black, by.coerceIn(0f, 1f))

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val inv = 1f - fraction
    return Color(
        red = start.red * inv + end.red * fraction,
        green = start.green * inv + end.green * fraction,
        blue = start.blue * inv + end.blue * fraction,
        alpha = start.alpha * inv + end.alpha * fraction
    )
}

// ── Palette centralisée — accès via LocalAppColors.current ──────────────────

data class AppColorScheme(
    val name: String,

    // Couleur principale (marque)
    val primary: Color,
    val onPrimary: Color,
    val primaryDisabled: Color,

    // Secondaire
    val secondary: Color,
    val onSecondary: Color,

    // Arrière-plans
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val darkSurface: Color,
    val lightSurface: Color,

    // Saisies & Formulaires
    val inputBackground: Color,
    val darkInput: Color,
    val lightInput: Color,
    val inputUnderline: Color,

    // Textes
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val textLabel: Color,

    // États (erreur, succès, avertissement, info)
    val errorRed: Color,
    val successGreen: Color,
    val warningOrange: Color,
    val infoBlue: Color,

    // Éléments UI
    val divider: Color,
    val border: Color,
    val buttonBackground: Color,
    val buttonText: Color,
    val buttonDisabledBackground: Color,
    val buttonDisabledText: Color,

    // Localisation & Carte
    val locationMarkerBlue: Color,
    val locationMarkerBlueDark: Color,
    val locationBlueLight: Color,
    val locationCirclePrecision: Color,

    // Réseaux sociaux
    val facebookBlue: Color,
    val googleRed: Color,

    // Nuances de gris
    val greyHint: Color,
    val greyLabel: Color,
    val dividerGrey: Color,

    // Bannières / champs d'erreur (Material error container)
    val errorContainer: Color,
    val onErrorContainer: Color,

    // Surfaces écrans sombres (cartes, barres, feuilles modales)
    val surfaceElevated: Color,
    val surfaceMuted: Color,
    /** Carte type adresse avec léger voile (alpha) */
    val surfaceOverlaySemi: Color,

    // Texte (nuances supplémentaires)
    val textTertiary: Color,
    val textDisabled: Color,
    val textCaption: Color,

    // Détails UI (poignée sheet, contours mode clair)
    val dragHandle: Color,
    val outlineLight: Color
)

// Fournisseur Compose — utilisez LocalAppColors.current dans n'importe quel Composable
val LocalAppColors = staticCompositionLocalOf<AppColorScheme> { GreenSchemeDark }
