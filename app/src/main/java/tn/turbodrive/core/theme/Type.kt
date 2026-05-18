package tn.turbodrive.core.theme

import androidx.compose.material3.Typography

// Material3 Typography bridge — maps TurboDrive tokens → Material3 slots.
// R-4.2 : `labelLarge` mapped to the new `button` style (M3 CTA semantic).
val TurboDriveTypography =
    Typography(
        headlineLarge = AppTypography.displayLarge,
        headlineMedium = AppTypography.displayMedium,
        titleLarge = AppTypography.headingL,
        titleMedium = AppTypography.headingM,
        titleSmall = AppTypography.headingS,
        bodyLarge = AppTypography.bodyL,
        bodyMedium = AppTypography.bodyM,
        bodySmall = AppTypography.bodyS,
        labelLarge = AppTypography.button,
        labelMedium = AppTypography.labelM,
        labelSmall = AppTypography.labelS,
    )
