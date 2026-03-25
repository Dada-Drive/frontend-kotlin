package com.dadadrive.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DadaDriveDarkColors = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = GreyLabel,
    onSecondary = White,
    background = Black,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    error = ErrorRed,
    onError = White
)

@Composable
fun DadaDriveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DadaDriveDarkColors,
        typography = DadaDriveTypography,
        content = content
    )
}
