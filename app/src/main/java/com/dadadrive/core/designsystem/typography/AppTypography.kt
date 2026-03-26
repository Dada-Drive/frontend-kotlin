package com.dadadrive.core.designsystem.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────
// DESIGN SYSTEM – TYPOGRAPHY
// ─────────────────────────────────────────────────────────

/**
 * DadaDrive typography scale.
 *
 * Use [AppTypography] when constructing the [MaterialTheme], and rely on
 * [androidx.compose.material3.MaterialTheme.typography] inside composables.
 */
val AppTypography = Typography(
    // ── Display ───────────────────────────────────────────
    displayLarge = TextStyle(
        fontSize = 57.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 44.sp
    ),
    // ── Headline ──────────────────────────────────────────
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp
    ),
    // ── Title ─────────────────────────────────────────────
    titleLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    ),
    // ── Body ──────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    ),
    // ── Label ─────────────────────────────────────────────
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
