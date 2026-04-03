package com.dadadrive.core.designsystem.spacing

import androidx.compose.ui.unit.dp

/**
 * Spacing tokens matching Swift AppSpacing: xs=4, s=8, m=12, l=16, xl=24, xxl=32, xxxl=48.
 */
object AppSpacing {
    val xxs = 4.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp

    /** Legacy aliases for existing components */
    val sm = m
    val md = l
    val lg = xl

    val inputRadius = 12.dp
    val buttonRadius = 999.dp
    val buttonHeight = 54.dp
    val cardRadius = 16.dp
    val sheetRadius = 20.dp

    val screenHorizontal = 20.dp
    val screenVertical = 24.dp
}

object AppRadius {
    val s = 6.dp
    val m = 12.dp
    val l = 20.dp
    val full = 999.dp
}

object AppIconSize {
    val s = 16.dp
    val m = 24.dp
    val l = 32.dp
    val xl = 48.dp
}
