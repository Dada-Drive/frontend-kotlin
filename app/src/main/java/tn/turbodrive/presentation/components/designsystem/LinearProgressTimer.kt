package tn.turbodrive.presentation.components.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tn.turbodrive.core.designsystem.spacing.AppRadius
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppMotion
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

private val BarHeight = 4.dp
private const val TICK_INTERVAL_MS = 100L

/**
 * Linear countdown / fill timer bar — R-4.4.
 *
 * Used to show the 30s offer validity (draining) or post-send cooldown
 * (filling). Ported from `turbodrive_redesign/screens-driver.jsx`
 * (SentState/CooldownState, L479-560).
 *
 * Color transitions as progress changes :
 *   ≥ 40%  → successGreen (accent)
 *   20–40% → warningOrange
 *   < 20%  → errorRed
 *
 * @param durationMs Total timer duration in milliseconds
 * @param isRunning When false the timer is frozen at its current state
 * @param fillFromStart True for a filling bar (cooldown), false for a draining bar (sent)
 * @param onTick Optional callback fired each tick with remaining milliseconds
 * @param onFinish Callback fired when the timer reaches zero / completion
 * @param progressOverride Fixed progress [0f, 1f] used in tests to bypass the running timer;
 *   when non-null the timer is frozen at this value regardless of [isRunning]
 */
@Composable
fun LinearProgressTimer(
    durationMs: Long,
    modifier: Modifier = Modifier,
    isRunning: Boolean = true,
    fillFromStart: Boolean = false,
    onTick: ((remainingMs: Long) -> Unit)? = null,
    onFinish: () -> Unit = {},
    progressOverride: Float? = null,
) {
    val c = LocalAppColors.current

    var remainingMs by remember(durationMs) { mutableLongStateOf(durationMs) }

    if (progressOverride == null && isRunning) {
        LaunchedEffect(durationMs) {
            while (remainingMs > 0) {
                delay(TICK_INTERVAL_MS)
                remainingMs = (remainingMs - TICK_INTERVAL_MS).coerceAtLeast(0)
                onTick?.invoke(remainingMs)
            }
            onFinish()
        }
    }

    val progress =
        progressOverride
            ?: (remainingMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

    val fillRatio = if (fillFromStart) 1f - progress else progress

    val barColor: Color by animateColorAsState(
        targetValue =
            when {
                fillRatio >= 0.4f -> c.successGreen
                fillRatio >= 0.2f -> c.warningOrange
                else -> c.errorRed
            },
        animationSpec = tween(durationMillis = AppMotion.DURATION_NORMAL_MS),
        label = "timerBarColor",
    )

    val remainingSecs = (remainingMs / 1000).coerceAtLeast(0)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(BarHeight)
                    .background(c.surfaceMuted, RoundedCornerShape(AppRadius.full)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(fillRatio.coerceIn(0f, 1f))
                        .height(BarHeight)
                        .background(barColor, RoundedCornerShape(AppRadius.full)),
            )
        }
        if (progressOverride == null || progressOverride > 0f) {
            Text(
                text = "${remainingSecs}s",
                style = AppTypography.monoM.copy(fontSize = AppTypography.labelS.fontSize),
                color = barColor,
            )
        }
    }
}

@Composable
internal fun LinearProgressTimerPreviewContent() {
    val c = LocalAppColors.current
    Surface(color = c.surface) {
        Column(
            modifier = Modifier.padding(AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
        ) {
            LinearProgressTimer(durationMs = 30_000, progressOverride = 1.0f)
            LinearProgressTimer(durationMs = 30_000, progressOverride = 0.5f)
            LinearProgressTimer(durationMs = 30_000, progressOverride = 0.05f)
        }
    }
}
