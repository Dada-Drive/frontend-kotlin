package com.dadadrive.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography tokens matching Swift AppFont exactly.
 *
 * Swift reference (AppTypography.swift):
 *   displayLarge  = 32 bold
 *   displayMedium = 26 bold
 *   headingL      = 22 semibold
 *   headingM      = 18 semibold
 *   headingS      = 16 semibold
 *   bodyL         = 16 regular
 *   bodyM         = 14 regular
 *   bodyS         = 12 regular
 *   labelL        = 14 medium
 *   labelM        = 12 medium
 *   labelS        = 10 medium
 *   monoL         = 28 bold monospaced
 *   monoM         = 20 medium monospaced
 */
object AppTypography {

    val displayLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
    )
    val displayMedium = TextStyle(
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
    )

    val headingL = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Default,
    )
    val headingM = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Default,
    )
    val headingS = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Default,
    )

    val bodyL = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Default,
    )
    val bodyM = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Default,
    )
    val bodyS = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Default,
    )

    val labelL = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Default,
    )
    val labelM = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Default,
    )
    val labelS = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Default,
    )

    val monoL = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
    )
    val monoM = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace,
    )
}
