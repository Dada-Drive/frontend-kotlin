package tn.turbodrive.presentation.components.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import tn.turbodrive.core.designsystem.motion.rememberReducedMotion
import tn.turbodrive.core.designsystem.tokens.AppMotion
import tn.turbodrive.core.theme.LocalAppColors

@Composable
fun DesignSpinnerInButton(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    CircularProgressIndicator(
        modifier = modifier.size(20.dp),
        color = c.onPrimary,
        strokeWidth = 2.dp,
    )
}

@Composable
fun DesignSpinnerStandalone(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        color = c.primary,
        strokeWidth = 2.dp,
    )
}

@Composable
fun LoadingDots(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    if (rememberReducedMotion()) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(c.primary, CircleShape),
                )
            }
        }
        return
    }
    val t = rememberInfiniteTransition(label = "dots")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { index ->
            val phase = index * (AppMotion.DURATION_NORMAL_MS / 3)
            val a by t.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(AppMotion.DURATION_NORMAL_MS, easing = LinearEasing, delayMillis = phase),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "d$index",
            )
            Box(
                Modifier
                    .size(8.dp)
                    .alpha(a)
                    .background(c.primary, CircleShape),
            )
        }
    }
}
