package tn.dadadrive.core.designsystem.tokens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.SpringSpec

/**
 * Motion tokens (design-system.md §9).
 */
object AppMotion {
    const val DURATION_FAST_MS = 150
    const val DURATION_NORMAL_MS = 250
    const val DURATION_SLOW_MS = 400

    val springDefault: SpringSpec<Float> = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)

    val springLow: SpringSpec<Float> = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
}
