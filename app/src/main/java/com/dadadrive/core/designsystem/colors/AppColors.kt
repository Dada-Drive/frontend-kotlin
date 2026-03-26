package com.dadadrive.core.designsystem.colors

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────
// DESIGN SYSTEM – COLORS
// ─────────────────────────────────────────────────────────

/**
 * Centralized color palette for DadaDrive.
 *
 * Prefer using [androidx.compose.material3.MaterialTheme.colorScheme] for
 * theme-aware colors inside composables. Use these tokens when building
 * the [MaterialTheme] color schemes or when you need raw color values
 * outside of a composable context.
 */
object AppColors {

    // ── Brand ─────────────────────────────────────────────
    val Brand = Color(0xFF80C000)
    val BrandDisabled = Color(0xFFA8D860)

    // ── Neutrals ─────────────────────────────────────────
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val GreyHint = Color(0xFF888888)
    val GreyLabel = Color(0xFF9E9E9E)

    // ── Dark surface ──────────────────────────────────────
    val DarkBackground = Color(0xFF000000)
    val DarkSurface = Color(0xFF111111)
    val DarkInput = Color(0xFF1A1A1A)
    val DarkDivider = Color(0xFF2A2A2A)
    val DarkInputUnderline = Color(0xFF444444)

    // ── Light surface ─────────────────────────────────────
    val LightBackground = Color(0xFFFFFFFF)
    val LightSurface = Color(0xFFF5F5F5)
    val LightInput = Color(0xFFEEEEEE)
    val LightDivider = Color(0xFFBBBBBB)

    // ── Semantic ──────────────────────────────────────────
    val Error = Color(0xFFE53935)
    val Success = Color(0xFF43A047)
    val Warning = Color(0xFFFB8C00)
    val Info = Color(0xFF1E88E5)

    // ── Social ────────────────────────────────────────────
    val FacebookBlue = Color(0xFF1877F2)
    val GoogleRed = Color(0xFFEA4335)
}
