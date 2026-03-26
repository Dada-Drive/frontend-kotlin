package com.dadadrive.core.designsystem.spacing

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────
// DESIGN SYSTEM – SPACING
// ─────────────────────────────────────────────────────────

/**
 * Spacing tokens used throughout the DadaDrive design system.
 *
 * Use these constants instead of raw [Dp] values to ensure consistent
 * spacing across the entire app.
 *
 * Scale (base-4):
 *  none  =  0 dp
 *  xxs   =  2 dp
 *  xs    =  4 dp
 *  sm    =  8 dp
 *  md    = 12 dp
 *  lg    = 16 dp
 *  xl    = 24 dp
 *  xxl   = 32 dp
 *  xxxl  = 48 dp
 *  huge  = 64 dp
 */
object AppSpacing {
    val none: Dp = 0.dp
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
    val huge: Dp = 64.dp

    // ── Screen-level padding ──────────────────────────────
    /** Standard horizontal screen padding. */
    val screenHorizontal: Dp = 24.dp
    /** Standard vertical screen padding. */
    val screenVertical: Dp = 16.dp

    // ── Component-specific ────────────────────────────────
    val buttonHeight: Dp = 56.dp
    val buttonRadius: Dp = 28.dp
    val inputRadius: Dp = 12.dp
    val cardRadius: Dp = 16.dp
    val iconSizeSmall: Dp = 16.dp
    val iconSizeMedium: Dp = 24.dp
    val iconSizeLarge: Dp = 32.dp
}
