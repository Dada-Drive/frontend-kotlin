package tn.dadadrive.presentation.components.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import tn.dadadrive.core.designsystem.motion.rememberReducedMotion
import tn.dadadrive.core.designsystem.spacing.AppRadius
import tn.dadadrive.core.designsystem.tokens.AppMotion
import tn.dadadrive.core.theme.LocalAppColors

@Composable
fun SkeletonRow(modifier: Modifier = Modifier) {
    SkeletonBlock(modifier.fillMaxWidth().height(16.dp), RoundedCornerShape(AppRadius.s))
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    SkeletonBlock(modifier.fillMaxWidth().height(120.dp), RoundedCornerShape(AppRadius.xl))
}

@Composable
fun SkeletonAvatar(
    modifier: Modifier = Modifier,
    sizeDp: androidx.compose.ui.unit.Dp = 40.dp
) {
    SkeletonBlock(modifier.size(sizeDp), CircleShape)
}

@Composable
fun SkeletonButton(modifier: Modifier = Modifier) {
    SkeletonBlock(modifier.fillMaxWidth().height(56.dp), RoundedCornerShape(AppRadius.full))
}

@Composable
fun SkeletonTextLine(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val w = maxWidth * widthFraction
        SkeletonBlock(Modifier.size(w, 14.dp), RoundedCornerShape(AppRadius.s))
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier, shape: Shape) {
    val c = LocalAppColors.current
    if (rememberReducedMotion()) {
        Box(
            modifier = modifier
                .background(c.surfaceMuted, shape)
                .alpha(0.5f)
        )
    } else {
        val transition = rememberInfiniteTransition(label = "skel")
        val alpha by transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(AppMotion.DURATION_SLOW_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "skelA"
        )
        Box(
            modifier = modifier
                .background(c.surfaceMuted, shape)
                .alpha(alpha)
        )
    }
}

@Composable
fun SkeletonTextBlock(modifier: Modifier = Modifier) {
    Column(modifier) {
        SkeletonTextLine(widthFraction = 1f)
        Spacer(Modifier.height(8.dp))
        SkeletonTextLine(widthFraction = 0.75f)
        Spacer(Modifier.height(8.dp))
        SkeletonTextLine(widthFraction = 0.6f)
    }
}
