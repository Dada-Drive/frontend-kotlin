package com.dadadrive.ui.theme

import androidx.compose.material3.Typography

// Material3 Typography bridge — maps DadaDrive tokens → Material3 slots
val DadaDriveTypography = Typography(
    headlineLarge  = AppTypography.displayLarge,
    headlineMedium = AppTypography.displayMedium,
    titleLarge     = AppTypography.headingL,
    titleMedium    = AppTypography.headingM,
    titleSmall     = AppTypography.headingS,
    bodyLarge      = AppTypography.bodyL,
    bodyMedium     = AppTypography.bodyM,
    bodySmall      = AppTypography.bodyS,
    labelLarge     = AppTypography.labelL,
    labelMedium    = AppTypography.labelM,
    labelSmall     = AppTypography.labelS
)