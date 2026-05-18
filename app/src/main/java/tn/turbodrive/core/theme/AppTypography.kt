package tn.turbodrive.core.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Typography tokens aligned on design-system.md §3 (TurboDrive v2 redesign).
 *
 * R-4.2 migration : sizes reduced 1-4sp from v1 (legacy mirrored Swift AppFont),
 * Inter font family substituted for FontFamily.Default, letterSpacing prescribed
 * in em (scales naturally with fontSize via TypographyScale), lineHeight set
 * per spec (1.2 for displays/mono, 1.3 for headings, 1.4 for labels, 1.5 for body).
 *
 * 3 new styles added beyond the v1 inventory : button, bodyStrong, smallStr.
 *
 * Mono* styles keep FontFamily.Monospace for OTP / plate number readability.
 */
object AppTypography {
    val displayLarge =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 38.4.sp,
            letterSpacing = (-0.025).em,
        )
    val displayMedium =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 33.6.sp,
            letterSpacing = (-0.02).em,
        )

    val headingL =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 31.2.sp,
            letterSpacing = (-0.02).em,
        )
    val headingM =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 26.sp,
            letterSpacing = (-0.015).em,
        )
    val headingS =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 22.1.sp,
            letterSpacing = (-0.01).em,
        )

    val bodyL =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 25.5.sp,
            letterSpacing = 0.em,
        )
    val bodyM =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.5.sp,
            letterSpacing = 0.em,
        )
    val bodyS =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 19.5.sp,
            letterSpacing = 0.em,
        )

    val labelL =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 21.sp,
            letterSpacing = 0.em,
        )
    val labelM =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.2.sp,
            letterSpacing = 0.em,
        )
    val labelS =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 15.4.sp,
            letterSpacing = 0.em,
        )

    /** R-4.2 new style : CTA button text (Material3 labelLarge slot). */
    val button =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 21.sp,
            letterSpacing = (-0.005).em,
        )

    /** R-4.2 new style : emphasized body text (semibold variant of bodyM). */
    val bodyStrong =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 22.5.sp,
            letterSpacing = 0.em,
        )

    /** R-4.2 new style : emphasized caption (semibold variant of bodyS). */
    val smallStr =
        TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 19.5.sp,
            letterSpacing = 0.em,
        )

    val monoL =
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 33.6.sp,
            letterSpacing = 0.em,
        )
    val monoM =
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 26.4.sp,
            letterSpacing = 0.em,
        )
}
