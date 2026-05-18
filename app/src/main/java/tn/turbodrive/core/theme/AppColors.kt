package tn.turbodrive.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Full app color scheme (brand + surfaces + text). Built per light/dark in [ColorSchemes.kt].
 */
@Immutable
data class AppColorScheme(
    val name: String,
    val primary: Color,
    val onPrimary: Color,
    val primaryDisabled: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val darkSurface: Color,
    val lightSurface: Color,
    val inputBackground: Color,
    val darkInput: Color,
    val lightInput: Color,
    val inputUnderline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val textLabel: Color,
    val errorRed: Color,
    val successGreen: Color,
    val warningOrange: Color,
    val infoBlue: Color,
    val divider: Color,
    val border: Color,
    val buttonBackground: Color,
    val buttonText: Color,
    val buttonDisabledBackground: Color,
    val buttonDisabledText: Color,
    val locationMarkerBlue: Color,
    val locationMarkerBlueDark: Color,
    val locationBlueLight: Color,
    val locationCirclePrecision: Color,
    val facebookBlue: Color,
    val googleRed: Color,
    val greyHint: Color,
    val greyLabel: Color,
    val dividerGrey: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val successContainer: Color,
    val surfaceElevated: Color,
    val surfaceMuted: Color,
    val surfaceOverlaySemi: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val textCaption: Color,
    val dragHandle: Color,
    val outlineLight: Color,
    val ratingYellow: Color,
    val coinSilver: Color,
    val coinGold: Color,
    // ── R-4.5 v2 design tokens ──────────────────────────────────────────────
    val accentInk: Color,
    val surfaceDeep: Color,
    val inkSoft: Color,
    val inkSubtle: Color,
    val warningSoft: Color,
    val infoSoft: Color,
    // ── R-4.5 map tokens ────────────────────────────────────────────────────
    val mapLand: Color,
    val mapWater: Color,
    val mapRoad: Color,
    val mapPath: Color,
)

val LocalAppColors = staticCompositionLocalOf<AppColorScheme> { TurboDriveSchemeLight }

/**
 * Swift-style fixed tokens + adaptive [background], [surface], [textPrimary] from [LocalAppColors]
 * (matches system/theme from [TurboDriveTheme]).
 */
object AppColor {
    val textHint: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.textHint

    val textOnGreen: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.onPrimary

    val green: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.successGreen

    val greenDisabled: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.primaryDisabled

    val error: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.errorRed

    val destination: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.errorRed

    val background: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.background

    val surface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.surface

    val textPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.textPrimary
}
