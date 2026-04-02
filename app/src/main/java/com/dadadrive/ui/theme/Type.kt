package com.dadadrive.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

object AppTypography {
    val displayLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)
    val displayMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold)

    val headingL = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    val headingM = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    val headingS = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

    val bodyL = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
    val bodyM = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
    val bodyS = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)

    val labelL = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
    val labelM = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
    val labelS = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium)

    val monoL = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
    )
    val monoM = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace
    )
}

val DadaDriveTypography = Typography(
    headlineLarge = AppTypography.displayLarge,
    headlineMedium = AppTypography.displayMedium,
    titleLarge = AppTypography.headingL,
    titleMedium = AppTypography.headingM,
    titleSmall = AppTypography.headingS,
    bodyLarge = AppTypography.bodyL,
    bodyMedium = AppTypography.bodyM,
    bodySmall = AppTypography.bodyS,
    labelLarge = AppTypography.labelL,
    labelMedium = AppTypography.labelM,
    labelSmall = AppTypography.labelS
)
