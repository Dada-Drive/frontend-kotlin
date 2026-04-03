package com.dadadrive.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Immutable
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

    val surfaceElevated: Color,
    val surfaceMuted: Color,
    val surfaceOverlaySemi: Color,

    val textTertiary: Color,
    val textDisabled: Color,
    val textCaption: Color,

    val dragHandle: Color,
    val outlineLight: Color
)

val LocalAppColors = staticCompositionLocalOf<AppColorScheme> { GreenSchemeDark }

/**
 * Swift-style fixed tokens + adaptive [background], [surface], [textPrimary] from [LocalAppColors]
 * (matches system/theme from [DadaDriveTheme]).
 */
object AppColor {
    val textHint: Color = Color(0xFF888888)
    val textOnGreen: Color = Color(0xFF111111)
    val green: Color = Color(0xFF80C000)
    val greenDisabled: Color = Color(0xFFA8D860)
    val error: Color = Color(0xFFE53935)
    val destination: Color get() = error

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
