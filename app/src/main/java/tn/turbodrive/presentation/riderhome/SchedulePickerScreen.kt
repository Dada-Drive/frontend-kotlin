package tn.turbodrive.presentation.riderhome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbodrive.R
import tn.turbodrive.core.designsystem.spacing.AppRadius
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors
import kotlin.math.abs

/**
 * S13 — Schedule picker standalone screen.
 *
 * Thin wrapper: renders [mapContent] behind, then shows [ScheduleForLaterSheet]
 * (the interactive wheel picker) as a modal overlay. Callers navigate here from
 * SearchDestinationScreen → "Programmer" action.
 *
 * Paparazzi tests snapshot [SchedulePickerContent] independently.
 */
@Composable
fun SchedulePickerScreen(
    onConfirm: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialEpochMs: Long? = null,
    mapContent: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        mapContent()
        ScheduleForLaterSheet(
            initialEpochMs = initialEpochMs,
            onDismiss = onBack,
            onConfirm = onConfirm,
        )
    }
}

@Composable
internal fun SchedulePickerContent(
    selectedLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val accent = colors.info

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surface,
        shape = RoundedCornerShape(topStart = AppRadius.xl, topEnd = AppRadius.xl),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier =
                Modifier.padding(
                    start = AppSpacing.screenH,
                    end = AppSpacing.screenH,
                    top = 14.dp,
                    bottom = 28.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Sheet handle
            Box(
                modifier =
                    Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(AppRadius.full))
                        .background(colors.divider),
            )

            Spacer(Modifier.height(14.dp))

            // Clock icon badge
            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .background(accent.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(AppIcon.clock),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.map_schedule_when_title),
                color = colors.textPrimary,
                style = AppTypography.headingS,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.map_schedule_min_lead),
                color = colors.textSecondary,
                style = AppTypography.bodyS,
            )

            Spacer(Modifier.height(18.dp))

            ScheduleStaticWheel(accent = accent)

            Spacer(Modifier.height(16.dp))

            // Selected time chip
            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(AppIcon.calendar),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = selectedLabel,
                    color = accent,
                    style = AppTypography.labelM,
                )
            }

            Spacer(Modifier.height(18.dp))

            // Confirm CTA
            Surface(
                onClick = onConfirm,
                shape = RoundedCornerShape(AppRadius.full),
                color = accent,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(AppSpacing.buttonHeight),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            painter = painterResource(AppIcon.check),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.map_schedule_confirm_time),
                            color = Color.White,
                            style = AppTypography.button,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Cancel
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    color = colors.textSecondary,
                    style = AppTypography.bodyM,
                )
            }
        }
    }
}

// Static visual representation of the wheel for Paparazzi snapshots.
// No LazyColumn/scroll — just fixed rows with graduated alpha.
@Composable
private fun ScheduleStaticWheel(accent: Color) {
    val colors = LocalAppColors.current
    val selectedIdx = 2
    val alphas = listOf(1f, 0.45f, 0.18f)
    val fontSizes = listOf(17.sp, 14.sp, 13.sp)
    val columns =
        listOf(
            2.1f to listOf("Hier", "Aujourd'hui", "Demain", "Sam. 11", "Dim. 12"),
            1f to listOf("12", "13", "14", "15", "16"),
            1f to listOf("00", "15", "30", "45", "00"),
            1f to listOf("AM", "PM", "AM", "PM", "AM"),
        )

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(colors.surfaceAlt, RoundedCornerShape(10.dp)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            columns.forEach { (weight, items) ->
                Column(
                    modifier = Modifier.weight(weight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items.forEachIndexed { index, item ->
                        val dist = abs(index - selectedIdx).coerceAtMost(2)
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = item,
                                color = colors.textPrimary,
                                fontSize = fontSizes[dist],
                                fontWeight = if (dist == 0) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.alpha(alphas[dist]),
                            )
                        }
                    }
                }
            }
        }
        // Center selection indicator (same style as ScheduleForLaterSheet)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp)
                    .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
        )
    }
}
