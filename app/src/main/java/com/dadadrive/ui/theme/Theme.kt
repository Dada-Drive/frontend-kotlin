package com.dadadrive.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DadaDriveDarkColors = darkColorScheme(
    primary = DadaDriveGreen,
    onPrimary = White,
    primaryContainer = DadaDriveGreen,
    onPrimaryContainer = White,
    secondary = GreyLabel,
    onSecondary = White,
    background = Black,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkInput,
    onSurfaceVariant = GreyLabel,
    outline = InputUnderline,
    error = ErrorRed,
    onError = White
)

private val DadaDriveLightColors = lightColorScheme(
    primary = DadaDriveGreen,
    onPrimary = White,
    primaryContainer = DadaDriveGreen,
    onPrimaryContainer = White,
    secondary = GreyLabel,
    onSecondary = Black,
    background = White,
    onBackground = Black,
    surface = LightSurface,
    onSurface = Black,
    surfaceVariant = LightInput,
    onSurfaceVariant = GreyLabel,
    outline = Color(0xFFBBBBBB),
    error = ErrorRed,
    onError = White
)

@Composable
fun DadaDriveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DadaDriveDarkColors else DadaDriveLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DadaDriveTypography,
        content = content
    )
}
