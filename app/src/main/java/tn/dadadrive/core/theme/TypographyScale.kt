package tn.dadadrive.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

internal fun TextStyle.scaleSp(multiplier: Float): TextStyle {
    if (multiplier == 1f) return this
    val scaledSize = (fontSize.value * multiplier).sp
    val scaledLineHeight = when {
        lineHeight.isUnspecified -> lineHeight
        lineHeight.type == TextUnitType.Sp -> (lineHeight.value * multiplier).sp
        else -> lineHeight
    }
    return copy(fontSize = scaledSize, lineHeight = scaledLineHeight)
}

fun dadaDriveTypographyScaled(multiplier: Float): Typography {
    if (multiplier == 1f) return DadaDriveTypography
    return Typography(
        headlineLarge = AppTypography.displayLarge.scaleSp(multiplier),
        headlineMedium = AppTypography.displayMedium.scaleSp(multiplier),
        titleLarge = AppTypography.headingL.scaleSp(multiplier),
        titleMedium = AppTypography.headingM.scaleSp(multiplier),
        titleSmall = AppTypography.headingS.scaleSp(multiplier),
        bodyLarge = AppTypography.bodyL.scaleSp(multiplier),
        bodyMedium = AppTypography.bodyM.scaleSp(multiplier),
        bodySmall = AppTypography.bodyS.scaleSp(multiplier),
        labelLarge = AppTypography.labelL.scaleSp(multiplier),
        labelMedium = AppTypography.labelM.scaleSp(multiplier),
        labelSmall = AppTypography.labelS.scaleSp(multiplier)
    )
}
