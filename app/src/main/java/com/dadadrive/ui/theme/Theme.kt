package com.dadadrive.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun DadaDriveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appColors: AppColorScheme = GreenSchemeDark,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                // Mode clair → icônes barre d’état / nav foncées ; mode sombre → icônes claires
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary             = appColors.primary,
            onPrimary           = appColors.onPrimary,
            primaryContainer    = appColors.primary,
            onPrimaryContainer  = appColors.onPrimary,
            secondary           = appColors.secondary,
            onSecondary         = appColors.onSecondary,
            background          = appColors.background,
            onBackground        = appColors.onBackground,
            surface             = appColors.darkSurface,
            onSurface           = appColors.textPrimary,
            surfaceVariant      = appColors.darkInput,
            onSurfaceVariant    = appColors.textLabel,
            outline             = appColors.inputUnderline,
            error               = appColors.errorRed,
            onError             = Color.White
        )
    } else {
        lightColorScheme(
            primary             = appColors.primary,
            onPrimary           = appColors.onPrimary,
            primaryContainer    = appColors.primary,
            onPrimaryContainer  = appColors.onPrimary,
            secondary           = appColors.secondary,
            onSecondary         = appColors.onSecondary,
            background          = appColors.background,
            onBackground        = appColors.onBackground,
            surface             = appColors.surface,
            onSurface           = appColors.textPrimary,
            surfaceVariant      = appColors.lightInput,
            onSurfaceVariant    = appColors.textLabel,
            outline             = appColors.outlineLight,
            error               = appColors.errorRed,
            onError             = Color.White
        )
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography  = DadaDriveTypography,
            content     = content
        )
    }
}
